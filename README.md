# VersionControlSystem

Софтуерен проект за управление на документи, версии и approval workflow. Репозиторието съдържа Spring Boot backend и React + TypeScript frontend.

## Кратко описание

Проектът реализира система за създаване, редакция, версиониране, преглед, сравняване, одобряване и публикуване на документи. Backend-ът е изграден със Spring Boot, Spring Security и JWT, а frontend-ът използва React, Vite, TypeScript и Tailwind CSS.

## Цел на проекта

Целта е да демонстрира цялостен модел за управление на документи с роли, permissions, workflow за review/approval и проследимост чрез audit logs.

## Основни функционалности

- Регистрация и логин с JWT токен
- Управление на потребители и роли
- Създаване и редакция на документи
- Ownership логика за автор и администратор
- Създаване и преглед на версии
- Сравняване на версии
- Approval, reject, publish и rollback workflow
- Архивиране и публикуване на документи
- PDF export на published и конкретни версии
- Audit logging на основни действия

## Използвани технологии

### Backend

- Java 21
- Spring Boot 3.5.11
- Spring Web
- Spring Security
- Spring Data JPA / Hibernate
- Flyway
- PostgreSQL
- JWT чрез `jjwt`
- OpenAPI / Swagger чрез `springdoc-openapi`
- PDF export чрез `openhtmltopdf`
- Unit, integration и security tests със Spring Boot Test и Mockito

### Frontend

- React 18
- TypeScript
- Vite
- React Router
- Axios
- TanStack React Query
- React Hook Form
- Zod
- Tailwind CSS

## Архитектура на проекта

Backend модулът следва стандартна layered структура:

- `controller` - REST endpoints и входни точки на API-то
- `service` - business logic, workflow правила и security annotations
- `repository` - JPA репозитории и заявки към базата
- `entity` - JPA entity класове и домейн модел
- `dto` - request/response модели за API контракт
- `security` - JWT, `UserDetails`, filters и handlers
- `exception` - централизиран error handling
- `config` - OpenAPI конфигурация
- `specification` - JPA specifications за филтриране

Подробна документация има в:

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
- [docs/API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md)
- [docs/DATABASE_MODEL.md](docs/DATABASE_MODEL.md)
- [docs/SECURITY.md](docs/SECURITY.md)
- [docs/TESTING.md](docs/TESTING.md)

## Описание на базата данни

Основните таблици са `users`, `roles`, `user_roles`, `documents`, `document_versions`, `approvals` и `audit_logs`. Моделът поддържа:

- many-to-many връзка между потребители и роли
- one-to-many връзка между документ и версии
- one-to-many връзка между версия и approvals
- one-to-many връзка между документ и неговия owner
- published version reference в документа

Повече детайли: [docs/DATABASE_MODEL.md](docs/DATABASE_MODEL.md)

## Основни функционалности по домейни

### Автентикация и потребители

- `POST /auth/login` и `POST /api/v1/tokens` издават JWT
- `POST /auth/register` и `POST /api/v1/users` създават нов потребител с роля `READER`
- `GET /auth/me` и `GET /api/v1/users/me` връщат текущия authenticated user
- `PATCH /api/v1/users/{id}/role` и legacy `PATCH /users/{id}/role` сменят роля само за `ADMIN`

### Документи и версии

- `AUTHOR` и `ADMIN` могат да създават и редактират документи
- `AUTHOR` и `ADMIN` могат да създават версии и да ги submit-ват за review
- `REVIEWER` и `ADMIN` могат да approve/reject/publish/rollback версии
- `READER` има достъп до published съдържание

### PDF export

- system-ът генерира PDF за published версията на документ
- генерира PDF и за конкретна версия, когато е налична и принадлежи на съответния документ

### Audit log

- основните операции записват audit записи в `audit_logs`
- API за преглед на audit log не е наличен

## Security документация

Проектът използва stateless JWT bearer authentication.

- Public endpoints: register, login, token issuance, Swagger UI и OpenAPI docs, както и OPTIONS requests
- Protected endpoints: всички останали API routes
- Роли: `ADMIN`, `AUTHOR`, `REVIEWER`, `READER`
- Подробности за auth flow, permissions и protected/public endpoints: [docs/SECURITY.md](docs/SECURITY.md)

## Workflow документация

Ключовите сценарии са описани в [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md), [docs/SECURITY.md](docs/SECURITY.md) и [docs/DATABASE_MODEL.md](docs/DATABASE_MODEL.md):

- автор създава документ
- автор редактира свой документ
- автор създава версия и я изпраща за review
- reviewer одобрява или отхвърля версия
- reviewer публикува approved версия
- reader преглежда published документи
- admin управлява роли и има пълен достъп

## Тестване

Проектът съдържа:

- controller authorization tests
- security integration tests
- service method security tests
- unit tests за auth, document, version, approval, PDF export и user логика
- specification tests
- context load smoke test

Подробности и команди: [docs/TESTING.md](docs/TESTING.md)

## Стартиране на проекта

### Изисквания

- Java 21
- Maven wrapper или Maven
- PostgreSQL 15 или съвместима версия
- Node.js 18+ за frontend-а

### Локална настройка

- Backend default datasource: `jdbc:postgresql://localhost:5433/vcs_sap_db`
- Default user: `postgres`
- Default password: `password`
- JWT secret и expiration могат да се override-нат чрез environment variables
- Frontend API URL: `VITE_API_URL=http://localhost:8080/api`

### Стартиране през Maven

1. Стартирайте PostgreSQL чрез `docker-compose.yml` или собствен инстанс.
2. Отворете папката `server`.
3. Изпълнете `.\mvnw.cmd spring-boot:run` на Windows или `./mvnw spring-boot:run` в Unix shell.

### Стартиране през IDE

1. Импортирайте `server/pom.xml` като Maven проект.
2. Стартирайте `com.sap.vcs.server.ServerApplication`.
3. Уверете се, че PostgreSQL и environment variables са налични.

### Достъп до API

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI spec: `http://localhost:8080/v3/api-docs`
- Health/test endpoint: `GET /test`

### Environment variables

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SERVER_PORT`
- `APP_JWT_SECRET`
- `APP_JWT_EXPIRATION_MS`
- `VITE_API_URL` за frontend-а

## Git workflow

В репото присъстват `main` и `develop`, а текущият работен клон е `develop`.

- Работете в отделен `feature/*` клон
- Синхронизирайте редовно с `develop`
- Commit-вайте малки и смислени промени
- Отваряйте pull request към `develop`
- При merge conflicts решавайте локално, без да презаписвате чужди промени

## Заключение

Проектът реализира пълна система за document version control с роли, JWT security, review workflow, PDF export и audit logging. Като бъдещи подобрения могат да се добавят refresh token flow, audit log API, password reset, attachment storage и по-богат approval metadata workflow.

## Подробна документация

- [docs/API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md)
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
- [docs/DATABASE_MODEL.md](docs/DATABASE_MODEL.md)
- [docs/SECURITY.md](docs/SECURITY.md)
- [docs/TESTING.md](docs/TESTING.md)
