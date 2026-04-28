# Тестване

## Видове тестове в проекта

### Security integration tests

- `SecurityAuthenticationIntegrationTest` - JWT authentication flow, invalid token, inactive user, insufficient role

### Controller authorization tests

- `AuthControllerAuthorizationTest`
- `UserControllerAuthorizationTest`
- `DocumentControllerAuthorizationTest`
- `DocumentVersionControllerAuthorizationTest`
- `ApprovalControllerAuthorizationTest`
- `VersionWorkflowControllerAuthorizationTest`
- `DocumentPdfControllerAuthorizationTest`
- `LegacyUserControllerAuthorizationTest`
- `TokenControllerAuthorizationTest`

Тези тестове проверяват дали endpoint-ите са публични, auth-only или role-restricted.

### Service method security tests

- `ServiceMethodSecurityTest`

Проверява `@PreAuthorize` правилата на service ниво за:

- create document
- create version
- approve/reject
- publish/rollback

### Unit tests

- `AuthenticatedUserServiceTest`
- `AuthServiceTest`
- `ApprovalServiceTest`
- `DocumentServiceTest`
- `DocumentVersionServiceTest`
- `PdfExportServiceTest`
- `UserServiceTest`

### Specification tests

- `DocumentSpecificationTest`

### Smoke test

- `ServerApplicationTests` - context load test

## Какво покриват тестовете

- `401 Unauthorized` при липсваща, невалидна или неактивна authentication
- `403 Forbidden` при валидна authentication, но недостатъчни права
- бизнес правила за document/version workflow
- ownership и visibility логика
- comparison и publish constraints
- PDF export behavior
- current user lookup behavior

## Как се стартират тестовете

### Windows PowerShell

От папката `server`:

```powershell
.\mvnw.cmd test
```

### Unix shells

```bash
./mvnw test
```

## Полезни команди

- Full suite: `.\mvnw.cmd test`
- Backend run: `.\mvnw.cmd spring-boot:run`
- Single test class: `.\mvnw.cmd "-Dtest=DocumentControllerAuthorizationTest" test`

## Препоръчителни тестови групи

- Security/auth: `SecurityAuthenticationIntegrationTest` и controller authorization tests
- Workflow: `ApprovalControllerAuthorizationTest` и `VersionWorkflowControllerAuthorizationTest`
- Business logic: `DocumentServiceTest`, `DocumentVersionServiceTest`, `ApprovalServiceTest`

## Бележки

- Няма отделен repository test package
- Текущият test stack използва Spring Boot Test, Spring Security Test и Mockito
- `spring.flyway.enabled=false` се използва в smoke test-а, за да не зависи от база данни