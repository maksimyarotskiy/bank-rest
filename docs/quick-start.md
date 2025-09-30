# Краткое руководство по запуску

## Шаг 1. Запуск через Docker Compose

```bash
docker-compose up -d --build
```

Будут запущены:
- PostgreSQL (порт 5432)
- Приложение (порт 8080)

## Шаг 2. Проверка API

```bash
# Регистрация
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@bank.com"
  }'

# Логин (получите токен из ответа)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'

# Создание карты
curl -X POST http://localhost:8080/api/cards \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "cardHolderName": "John Doe",
    "expiryDate": "2027-12-31",
    "initialBalance": 1000.00
  }'
```

## Шаг 3. Документация API

Откройте в браузере:
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI: http://localhost:8080/api-docs

## Дополнительно

### Режим разработки
```bash
./mvnw spring-boot:run
```

### Только база данных
```bash
docker run -d \
  -e POSTGRES_PASSWORD=bankpass \
  -e POSTGRES_USER=bankuser \
  -e POSTGRES_DB=bankdb \
  --name bank-postgres -p 5432:5432 postgres:15-alpine
```

## Краткая проверка возможностей

- Аутентификация: /api/auth/login, /api/auth/register
- Карты: /api/cards (список, создание, удаление, смена статуса)
- Переводы: /api/transfers (между своими картами)
- Админ: /api/admin/*
- Безопасность: JWT, шифрование, маскирование номера карты
