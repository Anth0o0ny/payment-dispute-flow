# Payment Dispute Flow

`Payment Dispute Flow` - демонстрационный backend-проект про мониторинг транзакций и обработку подозрительных операций.

Проект состоит из двух Kotlin/Spring Boot сервисов:

- `transaction-service` - сервис первичной обработки операций. Принимает транзакции, быстро оценивает риск по локальным правилам без вызова внешних сервисов и сохраняет только подозрительные операции.
- `dispute-workflow-service` - сервис углубленной проверки. Будет читать события о подозрительных операциях, запускать BPMN-процесс и собирать дополнительные данные для решения.

Идея проекта: отделить быстрый поток обработки транзакций от более тяжелой проверки подозрительных операций. Первый сервис должен оставаться простым и быстрым, а оркестрация проверок, внешние запросы и дальнейшие решения выносятся в отдельные компоненты.

## Текущая структура

```text
transaction-service/
dispute-workflow-service/
```

Сейчас в проекте подготовлен базовый multi-module Gradle-каркас:

- Kotlin;
- Spring Boot;
- Gradle Kotlin DSL;
- отдельные настройки для двух сервисов;
- порты `8081` и `8082`.

## Локальный запуск

Собрать проект:

```bash
./gradlew build
```

Запустить сервис операций:

```bash
docker compose up -d postgres
./gradlew :transaction-service:bootRun
```

Запустить сервис разбора споров:

```bash
./gradlew :dispute-workflow-service:bootRun
```

## Локальная инфраструктура

Для локальной разработки используется Docker Compose:

- `postgres` - операционное хранилище подозрительных операций и будущих статусов споров;
- `kafka` - брокер событий между сервисами;
- `kafka-ui` - веб-интерфейс для просмотра топиков и сообщений Kafka.

Запустить инфраструктуру:

```bash
docker compose up -d
```

Локальные подключения:

```text
Postgres JDBC: jdbc:postgresql://localhost:5432/payment_disputes
Postgres user: payment_app
Postgres password: payment_app
Kafka bootstrap для приложений на хосте: localhost:9094
Kafka bootstrap внутри Docker-сети: kafka:9092
Kafka UI: http://localhost:8085
```

Проверить Postgres:

```bash
docker compose exec postgres psql -U payment_app -d payment_disputes -c "select current_database();"
```

Проверить Kafka:

```bash
docker compose exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```

## `transaction-service`

`transaction-service` отвечает за первичную обработку операций клиента. Сервис не является источником полной истории транзакций: такая история считается внешней системой. На текущем этапе он предоставляет REST API для имитации входящего потока операций, рассчитывает `riskScore` и сохраняет в PostgreSQL только подозрительные операции, которые требуют дальнейшей проверки.

Если операция проходит первичную проверку без подозрений, сервис возвращает результат скоринга, но не пишет эту операцию в свою базу. Это снижает нагрузку на реляционное хранилище и оставляет в нем только данные, нужные для дальнейшего dispute/workflow-процесса.

База `payment_disputes` создается Postgres-контейнером при первом запуске. Таблицы приложения создаются Hibernate при старте `transaction-service` по JPA entity.

Структура модуля:

```text
transaction-service/src/main/kotlin/com/payflow/disputes/transaction/
  api/
    controller/ REST-контроллеры
    dto/        модели входящих запросов и ответов API
    error/      обработка ошибок REST API
  domain/       доменная модель операции и статусы
  repository/   интерфейс репозитория, JPA entity и Spring Data JPA реализация для suspicious-хранилища
  service/      бизнес-логика создания операций и первичная risk-оценка
    risk/       сервис скоринга и входные модели проверки
      rule/     отдельные правила первичной risk-оценки
```

Доступные ручки:

```text
POST /api/transactions                  выполнить первичную проверку операции
GET  /api/suspicious-transactions       получить сохраненные подозрительные операции
GET  /api/suspicious-transactions/{id}  получить подозрительную операцию по идентификатору
```

### Проверка API

Запустить сервис:

```bash
docker compose up -d postgres
./gradlew :transaction-service:bootRun
```

Проверить обычную операцию:

```bash
curl -X POST http://localhost:8081/api/transactions \
  -H "Content-Type: application/json" \
  -d '{"accountId":"acc-1001","merchant":"Online Store","amount":12500,"currency":"rub","customerAge":34,"channel":"mobile"}'
```

Пример ответа:

```json
{
  "id": "4364a50b-f897-4395-98a6-92e64b50ef53",
  "accountId": "acc-1001",
  "merchant": "Online Store",
  "amount": 12500,
  "currency": "RUB",
  "customerAge": 34,
  "channel": "MOBILE",
  "riskScore": 0,
  "riskReasons": [],
  "status": "NEW",
  "createdAt": "2026-07-04T12:48:36.900994Z"
}
```

Такая операция не сохраняется в PostgreSQL, потому что она не требует дальнейшей проверки.

Проверить подозрительную операцию:

```bash
curl -X POST http://localhost:8081/api/transactions \
  -H "Content-Type: application/json" \
  -d '{"accountId":"acc-2001","merchant":"Unknown Crypto Exchange","amount":150000,"currency":"usd","customerAge":76,"channel":"unknown"}'
```

Пример ответа:

```json
{
  "id": "7a0e30fe-2561-4a3c-b380-0122ff89d7f2",
  "accountId": "acc-2001",
  "merchant": "Unknown Crypto Exchange",
  "amount": 150000,
  "currency": "USD",
  "customerAge": 76,
  "channel": "UNKNOWN",
  "riskScore": 185,
  "riskReasons": [
    "HIGH_AMOUNT",
    "ELDERLY_CUSTOMER_TRANSFER",
    "RISKY_MERCHANT",
    "FOREIGN_CURRENCY",
    "UNKNOWN_CHANNEL"
  ],
  "status": "SUSPICIOUS",
  "createdAt": "2026-07-07T12:48:36.900994Z"
}
```

Такая операция сохраняется в PostgreSQL как suspicious snapshot для дальнейшей обработки.

Получить список сохраненных подозрительных операций:

```bash
curl http://localhost:8081/api/suspicious-transactions
```

Пример ответа:

```json
[
  {
    "id": "7a0e30fe-2561-4a3c-b380-0122ff89d7f2",
    "accountId": "acc-2001",
    "merchant": "Unknown Crypto Exchange",
    "amount": 150000,
    "currency": "USD",
    "customerAge": 76,
    "channel": "UNKNOWN",
    "riskScore": 185,
    "riskReasons": [
      "HIGH_AMOUNT",
      "ELDERLY_CUSTOMER_TRANSFER",
      "RISKY_MERCHANT",
      "FOREIGN_CURRENCY",
      "UNKNOWN_CHANNEL"
    ],
    "status": "SUSPICIOUS",
    "createdAt": "2026-07-07T12:48:36.900994Z"
  }
]
```

Получить подозрительную операцию по id:

```bash
curl http://localhost:8081/api/suspicious-transactions/7a0e30fe-2561-4a3c-b380-0122ff89d7f2
```

Пример ответа:

```json
{
  "id": "7a0e30fe-2561-4a3c-b380-0122ff89d7f2",
  "accountId": "acc-2001",
  "merchant": "Unknown Crypto Exchange",
  "amount": 150000,
  "currency": "USD",
  "customerAge": 76,
  "channel": "UNKNOWN",
  "riskScore": 185,
  "riskReasons": [
    "HIGH_AMOUNT",
    "ELDERLY_CUSTOMER_TRANSFER",
    "RISKY_MERCHANT",
    "FOREIGN_CURRENCY",
    "UNKNOWN_CHANNEL"
  ],
  "status": "SUSPICIOUS",
  "createdAt": "2026-07-04T12:48:36.900994Z"
}
```

Проверить валидацию:

```bash
curl -i -X POST http://localhost:8081/api/transactions \
  -H "Content-Type: application/json" \
  -d '{"accountId":"acc-1001","merchant":"Online Store","amount":0,"currency":"rub"}'
```

Пример ответа:

```text
HTTP/1.1 400
Content-Type: application/json

{"message":"amount must be positive","timestamp":"2026-07-04T12:48:36.988492Z"}
```

Проверить, что подозрительные операции сохранились в PostgreSQL:

```bash
docker compose exec postgres psql -U payment_app -d payment_disputes \
  -c "select id, account_id, amount, currency, risk_score, status from suspicious_transactions order by created_at desc;"
```

## План развития

1. Публиковать события о подозрительных операциях в Kafka.
2. Научить сервис разбора читать события из Kafka.
3. Добавить Camunda-процесс проверки подозрительной операции.
4. Возвращать итоговое решение в отдельный сервис принятия действий.
5. Добавить миграции БД через Flyway или Liquibase.
6. Добавить обработку ошибок, тесты и технический мониторинг.
