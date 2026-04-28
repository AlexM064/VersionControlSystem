# Security документация

## Как работи authentication

Проектът използва stateless JWT bearer authentication.

1. Потребителят изпраща `username` и `password` към login endpoint.
2. `AuthService` валидира credentials чрез `AuthenticationManager` и `DaoAuthenticationProvider`.
3. При успех се генерира JWT чрез `JwtService`.
4. JWT се връща на клиента в `AuthResponseDto`.
5. Клиентът изпраща токена в header:

```http
Authorization: Bearer <jwt>
```

6. `JwtAuthenticationFilter` извлича токена, проверява го и попълва `SecurityContextHolder`.

## Как се използва JWT

JWT съдържа:

- subject = username
- `roles` claim = списък от роли
- `enabled` claim = дали акаунтът е активен
- issuedAt и expiration

`JwtService` прави:

- token generation
- username extraction
- roles extraction
- enabled extraction
- token validation

## Какви роли има системата

- `ADMIN`
- `AUTHOR`
- `REVIEWER`
- `READER`

## Permissions по роля

### ADMIN

- пълен достъп до documents, versions, approvals и user role management
- може да създава, редактира, архивира и изтрива документи
- може да approve/reject/publish/rollback версии
- може да сменя роли на потребители

### AUTHOR

- може да регистрира нов потребител чрез public register endpoint
- може да създава и редактира собствени документи
- може да създава версии и да ги submit-ва за review
- може да чете документи и published съдържание според visibility правилата

### REVIEWER

- може да преглежда документи с workflow visibility
- може да approve/reject/publish/rollback версии
- не може да създава документи или версии

### READER

- може да вижда published content
- няма достъп до write endpoints за документи и версии

## Как се взима текущият authenticated user

Има два помощни подхода:

- `AuthenticatedUserService.getCurrentUsername()` чете username от `SecurityContextHolder`
- `AuthenticatedUserService.getCurrentUser()` зарежда `User` от `UserRepository`
- `DocumentVisibilityService.getCurrentAuthenticatedUser()` прави същото, но се използва за document workflow и visibility

Тези helper-и хвърлят грешка, ако няма authenticated user в security context.

## Public и protected endpoints

### Public endpoints

- `POST /auth/register`
- `POST /auth/login`
- `POST /api/v1/tokens`
- Swagger UI и OpenAPI docs
- `OPTIONS /**`

### Protected endpoints

Всички останали routes изискват authentication, а някои и конкретна роля.

## Route-level authorization matrix

### Auth / user

- `GET /auth/me` - authenticated
- `GET /api/v1/users/me` - authenticated
- `PATCH /api/v1/users/{id}/role` - `ADMIN`
- `PATCH /users/{id}/role` - `ADMIN`

### Documents

- `POST /documents`, `PUT /documents/{id}` - `AUTHOR` или `ADMIN`
- `PATCH /documents/{id}/archive` - `AUTHOR` или `ADMIN`
- `DELETE /documents/{id}` - `ADMIN`
- `GET /documents`, `GET /documents/{id}`, `GET /documents/{id}/history`, `GET /documents/{id}/versions`, `GET /documents/compare` - `AUTHOR`, `REVIEWER`, `ADMIN`
- `GET /documents/{id}/published-version`, `GET /documents/{id}/published-version/pdf`, `GET /documents/published`, `GET /documents/{id}/published-only` - `READER`, `AUTHOR`, `REVIEWER`, `ADMIN`

### Versions and workflow

- `POST /documents/{documentId}/versions` - `AUTHOR` или `ADMIN`
- `POST /documents/{documentId}/versions/{versionId}/submit` - `AUTHOR` или `ADMIN`
- `POST /versions/{id}/approve` - `REVIEWER` или `ADMIN`
- `POST /versions/{id}/reject` - `REVIEWER` или `ADMIN`
- `POST /versions/{versionId}/publish` - `REVIEWER` или `ADMIN`
- `POST /versions/{versionId}/rollback` - `REVIEWER` или `ADMIN`

### PDF export

- `GET /documents/{id}/published-version/pdf` - `READER`, `AUTHOR`, `REVIEWER`, `ADMIN`
- `GET /documents/{documentId}/versions/{versionId}/pdf` - `AUTHOR`, `REVIEWER`, `ADMIN`

## Security handlers

- `RestAuthenticationEntryPoint` връща `401 Unauthorized` при липсваща или невалидна authentication
- `RestAccessDeniedHandler` връща `403 Forbidden` при валидна authentication, но недостатъчни права

## CORS и session policy

- CSRF е изключен
- session management е `STATELESS`
- CORS е позволен за `http://localhost:3000`, `3001` и `3002`
- позволени са методите `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`

## Поддържани fallback правила

`SecurityConfig` завършва с `anyRequest().authenticated()`, така че всеки немапнат route остава защитен.

## Не е налично

- refresh token flow
- logout / token revocation
- password reset endpoint
- role CRUD API
- audit log browsing API
- Basic Auth authentication flow