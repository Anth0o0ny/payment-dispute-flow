# Payment Dispute Flow

`Payment Dispute Flow` - демонстрационный backend-проект про обработку спорных карточных операций.

Проект состоит из двух Kotlin/Spring Boot сервисов:

- `transaction-service` - сервис операций. Отвечает за данные по платежам и будет публиковать события о спорных операциях.
- `dispute-workflow-service` - сервис разбора споров. Будет запускать процесс проверки, получать детали операции и отправлять итоговое решение.

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
./gradlew :transaction-service:bootRun
```

Запустить сервис разбора споров:

```bash
./gradlew :dispute-workflow-service:bootRun
```

## `transaction-service`

`transaction-service` отвечает за операции клиента. На текущем этапе сервис хранит операции в памяти приложения и предоставляет REST API для создания и чтения операций.

Структура модуля:

```text
transaction-service/src/main/kotlin/com/payflow/disputes/transaction/
  api/          REST-контроллеры, DTO и обработка ошибок
  domain/       доменная модель операции и статусы
  repository/   интерфейс репозитория и in-memory реализация
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
./gradlew :transaction-service:bootRun
```

Создать операцию:

```bash
curl -X POST http://localhost:8081/api/transactions \
  -H "Content-Type: application/json" \
  -d '{"accountId":"acc-1001","merchant":"Online Store","amount":12500,"currency":"rub"}'
```

Пример ответа:

```json
{
  "id": "4364a50b-f897-4395-98a6-92e64b50ef53",
  "accountId": "acc-1001",
  "merchant": "Online Store",
  "amount": 12500,
  "currency": "RUB",
  "status": "NEW",
  "createdAt": "2026-07-04T12:48:36.900994Z"
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

## План развития

1. Добавить Kafka в локальную инфраструктуру.
2. Публиковать события о созданных спорах.
3. Научить сервис разбора споров читать события из Kafka.
4. Добавить Camunda-процесс проверки спорной операции.
5. Возвращать итоговое решение обратно в сервис операций.
