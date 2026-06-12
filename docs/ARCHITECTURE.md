# Архитектура на проекта

## Обща структура

Репозиторият е организиран като двуслойно приложение:

- `frontend/` - React + TypeScript клиент
- `server/` - Spring Boot backend
- `docs/` - проектна документация
- `docker-compose.yml` - локален PostgreSQL контейнер

### Backend пакетна структура

- `controller` - REST API слоеве
- `service` - business logic и workflow правила
- `repository` - JPA repositories
- `entity` - domain entities
- `entity/enums` - статични домейн статуси и типове
- `dto` - request/response contracts
- `security` - JWT и Spring Security конфигурация
- `exception` - глобално обработване на грешки
- `config` - OpenAPI конфигурация
- `specification` - JPA Specifications за филтриране

## Controller layer

Controller-ите са тънки и делегират почти цялата логика на service слоя.

- `AuthController` - legacy auth endpoints за register, login и me
- `TokenController` - versioned token issuance endpoint
- `UserController` - versioned user endpoints
- `LegacyUserController` - legacy role update endpoint
- `DocumentController` - document CRUD, filtering, history, compare и published access
- `DocumentVersionController` - version creation, listing и submit for review
- `ApprovalController` - approve/reject workflow
- `VersionWorkflowController` - publish/rollback workflow
- `DocumentPdfController` - PDF export
- `TestController` - health-like test endpoint

Всички controller-и са свързани със Swagger/OpenAPI чрез `springdoc-openapi`.

## Service layer

Service слоят съдържа основната бизнес логика:

- `AuthService` - login и JWT issuance
- `UserService` - register, me, role update, password helper methods
- `AuthenticatedUserService` - взимане на текущия authenticated user от security context
- `DocumentService` - create/update/archive/delete/read documents
- `DocumentVersionService` - create versions, submit for review, compare, publish, rollback
- `ApprovalService` - approve/reject decisions
- `PdfExportService` - HTML към PDF generation
- `AuditLogService` - запис в audit_logs
- `DocumentVisibilityService` - ownership, role-based visibility и access rules

Business rules са валидирани със `@PreAuthorize`, транзакции и custom exceptions.

## Repository layer

Репозиторите са стандартни Spring Data JPA интерфейси:

- `UserRepository`
- `RoleRepository`
- `DocumentRepository`
- `DocumentVersionRepository`
- `ApprovalRepository`
- `AuditLogRepository`

`DocumentRepository` използва `JpaSpecificationExecutor`, за да поддържа филтриране по title и status.

## DTO layer

DTO-тата отделят API контракта от entity моделите.

### Request DTOs

- `RegisterRequestDto`
- `LoginRequestDto`
- `DocumentRequestDto`
- `UpdateDocumentMetadataRequestDto`
- `DocumentVersionRequestDto`
- `UpdateUserRoleRequestDto`

### Response DTOs

- `AuthResponseDto`
- `MeResponseDto`
- `DocumentResponseDto`
- `DocumentVersionResponseDto`
- `DocumentHistoryResponseDto`
- `CompareVersionsResponseDto`
- `PublishDocumentResponseDto`
- `ApprovalResponseDto`

## Entity layer

Entity моделът покрива:

- потребители и роли
- документи и техните версии
- approvals за review workflow
- audit log записи

Основните relations са описани подробно в [DATABASE_MODEL.md](DATABASE_MODEL.md).

## Security layer

Security модулът е stateless и JWT-базиран.

- `SecurityConfig` - authorizations, CORS, CSRF off, method security, custom handlers
- `JwtService` - token generation и validation
- `JwtAuthenticationFilter` - извличане на bearer token и populating SecurityContext
- `JwtAuthenticationToken` - custom authentication object
- `CustomUserDetailsService` - bridge към `UserRepository`
- `SecurityUserDetails` - Spring Security adapter за `User`
- `RestAuthenticationEntryPoint` - 401 handler
- `RestAccessDeniedHandler` - 403 handler

## Exception handling

Грешките се обработват централно от `GlobalExceptionHandler`.

- `ResourceNotFoundException` -> 404
- `MethodArgumentNotValidException` -> 400
- `BusinessRuleViolationException` -> 409
- всички останали exception-и -> 500

Отговорът е `ErrorResponse`, който съдържа timestamp, status, error, message и path.

## JPA filtering и visibility

`DocumentSpecification` изгражда динамични филтри по title и status.

`DocumentVisibilityService` реализира следната логика:

- admin вижда всичко
- owner вижда собствения си документ и всички негови версии
- reviewer вижда workflow-visible документи и версии
- reader вижда published content

## API versioning

Проектът поддържа едновременно:

- legacy routes като `/auth`, `/documents`, `/versions`, `/users`
- versioned routes с prefix `/api/v1`

Това дава backward compatibility за клиентския код и поетапна миграция към versioned API.

## Място на тестовете

Тестовете са в `server/src/test/java/com/sap/vcs/server` и покриват:

- security flow
- controller authorization
- service method security
- document specification
- auth/user/service behavior
- application context load