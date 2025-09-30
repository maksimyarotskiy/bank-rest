# Bank REST API — банковские карты (тестовое задание)

Короткое описание: REST API на Spring Boot для управления банковскими картами, аутентификации (JWT) и переводов между своими картами. Код и названия сущностей — на английском; в README используется русский.

## Функционал

- Аутентификация/авторизация (JWT), роли: ADMIN и USER
- Управление картами: создание, просмотр, изменение статуса (ACTIVE/BLOCKED/EXPIRED), удаление
- Шифрование номера карты и маскирование в ответах
- Переводы между своими картами, история переводов
- Пагинация и фильтрация
- OpenAPI/Swagger UI

## Технологии

- Java 21, Spring Boot 3.5.x
- Spring Security (JWT), Spring Data JPA
- PostgreSQL, Liquibase (для миграций; может быть отключён в docker профиле)
- Docker, Docker Compose
- Swagger/OpenAPI 3

## Запуск

### Вариант 1 — Docker Compose (рекомендуется)

```bash
docker-compose up -d --build
```

Доступ:
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/api-docs

По умолчанию активен профиль `docker` (см. `SPRING_PROFILES_ACTIVE`), подключение к БД — контейнер `postgres`.

### Вариант 2 — локально

1) Настроить PostgreSQL (или изменить настройки в `application.properties`).
2) Запустить приложение:

```bash
./mvnw spring-boot:run
```

## Основные эндпойнты

- Аутентификация: `POST /api/auth/register`, `POST /api/auth/login`
- Карты: `GET /api/cards`, `POST /api/cards`, `GET /api/cards/{id}`, `PUT /api/cards/{id}/status`, `DELETE /api/cards/{id}`
- Переводы: `GET /api/transfers`, `POST /api/transfers`, `GET /api/transfers/{id}`
- Админ: `GET /api/admin/users`, `POST /api/admin/users`, `GET /api/admin/cards`, `POST /api/admin/cards`

Примечание: защищённые эндпойнты требуют заголовок `Authorization: Bearer <JWT>`.

## Конфигурация

Основные параметры:
- База данных: `spring.datasource.*`
- JWT: `jwt.secret`, `jwt.expiration`
- OpenAPI: `springdoc.*`

В Docker-профиле (`application-docker.properties`) может быть отключён Liquibase для упрощённого запуска.

## Краткие замечания по реализации

- Номер карты хранится в зашифрованном виде, в ответах — маска.
- Роли и доступ проверяются через Spring Security и JWT.
- Переводы возможны только между картами одного пользователя, с проверками статуса карты и достаточности средств.

## Тестирование

Запуск тестов:
```bash
./mvnw test
```

Swagger UI позволяет вручную проверить бизнес-сценарии (регистрация, логин, создание карты, перевод и т.д.).

---

Проект оформлен как тестовое задание: документация краткая, без маркетинговых формулировок и эмодзи, комментарии в коде — по делу.
