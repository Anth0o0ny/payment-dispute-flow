# Payment Dispute Flow

`Payment Dispute Flow` - демонстрационный backend-проект про мониторинг транзакций и обработку подозрительных операций.

Проект состоит из двух Kotlin/Spring Boot сервисов:

- `transaction-service` - сервис первичной обработки операций. Принимает транзакции, быстро оценивает риск по локальным правилам без вызова внешних сервисов, сохраняет короткий audit-результат для подозрительных операций и публикует событие в Kafka.
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
docker compose up -d postgres kafka kafka-ui
./gradlew :transaction-service:bootRun
```

Запустить сервис разбора споров:

```bash
./gradlew :dispute-workflow-service:bootRun
```

## Локальная инфраструктура

Для локальной разработки используется Docker Compose:

- `postgres` - операционное хранилище результатов первичного risk screening;
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

`transaction-service` отвечает за первичную обработку операций клиента. Сервис не является источником полной истории транзакций: такая история считается внешней системой. На текущем этапе он предоставляет REST API для имитации входящего потока операций, рассчитывает `riskScore`, сохраняет в PostgreSQL только короткий audit-результат проверки и публикует событие о подозрительной операции в Kafka.

Если операция проходит первичную проверку без подозрений, сервис возвращает результат скоринга, но не пишет эту операцию в свою базу и не отправляет событие в Kafka. Если операция подозрительная, Postgres хранит только результат screening: идентификатор audit-case, идентификатор проверенной операции, score, причины риска, решение и время проверки. Детали операции передаются во второй сервис через Kafka-событие.

База `payment_disputes` создается Postgres-контейнером при первом запуске. Таблицы приложения создаются Hibernate при старте `transaction-service` по JPA entity.

Структура модуля:

```text
transaction-service/src/main/kotlin/com/payflow/disputes/transaction/
  api/
    controller/ REST-контроллеры
    dto/        модели входящих запросов и ответов API
    error/      обработка ошибок REST API
  domain/       доменная модель операции и статусы
  messaging/    Kafka-адаптеры для публикации событий
  repository/   JPA entity и Spring Data JPA реализация audit-хранилища
  service/      бизнес-логика создания операций и первичная risk-оценка
    command/    входные команды service-слоя
    event/      события, которые публикует service-слой
    port/       порты service-слоя для хранилища и публикации событий
    risk/       сервис скоринга и входные модели проверки
      rule/     отдельные правила первичной risk-оценки
```

Доступные ручки:

```text
POST /api/transactions                  выполнить первичную проверку операции
GET  /api/risk-screening-cases       получить audit-записи первичного screening
GET  /api/risk-screening-cases/{id}  получить audit-запись по идентификатору
```

### Проверка API

Запустить сервис:

```bash
docker compose up -d postgres kafka kafka-ui
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

Такая операция не сохраняется в PostgreSQL и не публикуется в Kafka, потому что она не требует дальнейшей проверки.

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

По такой операции в PostgreSQL сохраняется короткий `risk_screening_case`, а полное событие публикуется в Kafka для дальнейшей обработки во втором сервисе.

Получить список audit-записей risk screening:

```bash
curl http://localhost:8081/api/risk-screening-cases
```

Пример ответа:

```json
[
  {
    "id": "e65926fb-c60e-4765-89a3-9ed835972467",
    "transactionId": "7a0e30fe-2561-4a3c-b380-0122ff89d7f2",
    "riskScore": 185,
    "riskReasons": [
      "HIGH_AMOUNT",
      "ELDERLY_CUSTOMER_TRANSFER",
      "RISKY_MERCHANT",
      "FOREIGN_CURRENCY",
      "UNKNOWN_CHANNEL"
    ],
    "decision": "REQUIRES_REVIEW",
    "screenedAt": "2026-07-07T12:48:36.900994Z"
  }
]
```

Получить audit-запись risk screening по id:

```bash
curl http://localhost:8081/api/risk-screening-cases/e65926fb-c60e-4765-89a3-9ed835972467
```

Пример ответа:

```json
{
  "id": "e65926fb-c60e-4765-89a3-9ed835972467",
  "transactionId": "7a0e30fe-2561-4a3c-b380-0122ff89d7f2",
  "riskScore": 185,
  "riskReasons": [
    "HIGH_AMOUNT",
    "ELDERLY_CUSTOMER_TRANSFER",
    "RISKY_MERCHANT",
    "FOREIGN_CURRENCY",
    "UNKNOWN_CHANNEL"
  ],
  "decision": "REQUIRES_REVIEW",
  "screenedAt": "2026-07-07T12:48:36.900994Z"
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

Проверить, что audit-запись сохранилась в PostgreSQL:

```bash
docker compose exec postgres psql -U payment_app -d payment_disputes \
  -c "select id, transaction_id, risk_score, decision, screened_at from risk_screening_cases order by screened_at desc;"
```

Проверить, что событие опубликовано в Kafka:

```bash
docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic suspicious-transactions.detected \
  --from-beginning \
  --max-messages 1
```

Пример сообщения:

```json
{
  "eventId": "f3a35c53-0dc5-40e6-83ed-d1d4f28b8207",
  "suspiciousTransactionId": "7a0e30fe-2561-4a3c-b380-0122ff89d7f2",
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
  "detectedAt": "2026-07-07T12:48:36.900994Z"
}
```

Тот же топик можно посмотреть через Kafka UI:

```text
http://localhost:8085
```

## План развития

1. Научить сервис разбора читать события из Kafka.
2. Добавить Camunda-процесс проверки подозрительной операции.
3. Возвращать итоговое решение в отдельный сервис принятия действий.
4. Добавить миграции БД через Flyway или Liquibase.
5. Добавить обработку ошибок, тесты и технический мониторинг.
