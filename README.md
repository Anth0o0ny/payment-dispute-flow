# Payment Dispute Flow

`Payment Dispute Flow` - демонстрационный backend-проект про мониторинг транзакций и обработку подозрительных операций.

Проект состоит из двух Kotlin/Spring Boot сервисов:

- `transaction-service` - сервис первичной обработки операций. Принимает транзакции, сохраняет их и быстро оценивает риск по локальным правилам без вызова внешних сервисов.
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

- `postgres` - база данных для операций и будущих статусов споров;
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

`transaction-service` отвечает за первичную обработку операций клиента. На текущем этапе сервис хранит операции в PostgreSQL, предоставляет REST API для создания и чтения операций, рассчитывает `riskScore` и присваивает статус `SUSPICIOUS`, если операция требует дальнейшей проверки.

База `payment_disputes` создается Postgres-контейнером при первом запуске. Таблицы приложения создаются Hibernate при старте `transaction-service` по JPA entity.

Структура модуля:

```text
transaction-service/src/main/kotlin/com/payflow/disputes/transaction/
  api/
    controller/ REST-контроллеры
    dto/        модели входящих запросов и ответов API
    error/      обработка ошибок REST API
  domain/       доменная модель операции и статусы
  repository/   интерфейс репозитория, JPA entity и Spring Data JPA реализация
  risk/         правила первичной risk-оценки операций
  service/      бизнес-логика создания и получения операций
```

Доступные ручки:

```text
POST /api/transactions       создать операцию
GET  /api/transactions       получить список операций
GET  /api/transactions/{id}  получить операцию по идентификатору
```

### Проверка API

Запустить сервис:

```bash
docker compose up -d postgres
./gradlew :transaction-service:bootRun
```

Создать операцию:

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

Создать подозрительную операцию:

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

Получить список операций:

```bash
curl http://localhost:8081/api/transactions
```

Пример ответа:

```json
[
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
]
```

Получить операцию по id:

```bash
curl http://localhost:8081/api/transactions/4364a50b-f897-4395-98a6-92e64b50ef53
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

Проверить, что операции сохранились в PostgreSQL:

```bash
docker compose exec postgres psql -U payment_app -d payment_disputes \
  -c "select id, account_id, amount, currency, risk_score, status from transactions order by created_at desc;"
```

## План развития

1. Публиковать события о подозрительных операциях в Kafka.
2. Научить сервис разбора читать события из Kafka.
3. Добавить Camunda-процесс проверки подозрительной операции.
4. Возвращать итоговое решение в отдельный сервис принятия действий.
5. Добавить миграции БД через Flyway или Liquibase.
6. Добавить обработку ошибок, тесты и технический мониторинг.
