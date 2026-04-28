# API документация

## Общи бележки

- Базовите legacy routes са без `/api/v1`
- Версионираните routes използват `/api/v1`
- Повечето защитени endpoints изискват header `Authorization: Bearer <jwt>`
- При валидационни грешки се връща `400 Bad Request`
- При липсващ ресурс се връща `404 Not Found`
- При business rule нарушение се връща `409 Conflict`
- При липса на права се връща `403 Forbidden`
- При липса на authentication се връща `401 Unauthorized`

## AuthController

Base path: `/auth`

| Endpoint | Method | Auth | Request body | Response body | Status codes | Описание |
| --- | --- | --- | --- | --- | --- | --- |
| `/register` | POST | Public | `RegisterRequestDto` | `AuthResponseDto` | `200`, `400`, `409`, `500` | Регистрация на нов потребител; създава `READER` и връща JWT |
| `/login` | POST | Public | `LoginRequestDto` | `AuthResponseDto` | `200`, `400`, `401`, `500` | Login и издаване на JWT |
| `/me` | GET | Authenticated | няма | `MeResponseDto` | `200`, `401`, `404`, `500` | Връща текущия authenticated user |

## TokenController

Base path: `/api/v1/tokens`

| Endpoint | Method | Auth | Request body | Response body | Status codes | Описание |
| --- | --- | --- | --- | --- | --- | --- |
| `/` | POST | Public | `LoginRequestDto` | `AuthResponseDto` | `200`, `400`, `401`, `500` | Издава JWT токен |

## UserController

Base path: `/api/v1/users`

| Endpoint | Method | Auth | Request body | Response body | Status codes | Описание |
| --- | --- | --- | --- | --- | --- | --- |
| `/` | POST | Public | `RegisterRequestDto` | `AuthResponseDto` | `200`, `400`, `409`, `500` | Регистрация на нов потребител |
| `/me` | GET | Bearer JWT | няма | `MeResponseDto` | `200`, `401`, `404`, `500` | Текущ потребител |
| `/{id}/role` | PATCH | `ADMIN` | `UpdateUserRoleRequestDto` | няма | `204`, `400`, `401`, `403`, `404`, `500` | Смяна на роля на потребител |

## LegacyUserController

Base path: `/users`

| Endpoint | Method | Auth | Request body | Response body | Status codes | Описание |
| --- | --- | --- | --- | --- | --- | --- |
| `/{id}/role` | PATCH | `ADMIN` | `UpdateUserRoleRequestDto` | няма | `204`, `400`, `401`, `403`, `404`, `500` | Legacy endpoint за смяна на роля |

## DocumentController

Base paths: `/documents` и `/api/v1/documents`

| Endpoint | Method | Auth | Request body | Response body | Status codes | Описание |
| --- | --- | --- | --- | --- | --- | --- |
| `/` | POST | `AUTHOR` или `ADMIN` | `DocumentRequestDto` | `DocumentResponseDto` | `201`, `400`, `401`, `403`, `500` | Създава нов документ |
| `/{id}` | PUT | `AUTHOR` или `ADMIN` | `UpdateDocumentMetadataRequestDto` | `DocumentResponseDto` | `200`, `400`, `401`, `403`, `404`, `409`, `500` | Обновява metadata на документ |
| `/{id}/archive` | PATCH | `AUTHOR` или `ADMIN` | няма | няма | `204`, `401`, `403`, `404`, `409`, `500` | Архивира документ |
| `/{id}` | DELETE | `ADMIN` | няма | няма | `204`, `401`, `403`, `404`, `500` | Перманентно изтрива документ |
| `/compare` | GET | `AUTHOR`, `REVIEWER`, `ADMIN` | query: `leftVersionId`, `rightVersionId` | `CompareVersionsResponseDto` | `200`, `401`, `403`, `404`, `409`, `500` | Сравнява две версии |
| `/` | GET | `AUTHOR`, `REVIEWER`, `ADMIN` | query: `title`, `status`, pageable | `Page<DocumentResponseDto>` | `200`, `401`, `403`, `500` | Връща документи с филтри и pagination |
| `/{id}` | GET | `AUTHOR`, `REVIEWER`, `ADMIN` | няма | `DocumentResponseDto` | `200`, `401`, `403`, `404`, `500` | Връща документ по id |
| `/{id}/history` | GET | `AUTHOR`, `REVIEWER`, `ADMIN` | няма | `List<DocumentHistoryResponseDto>` | `200`, `401`, `403`, `404`, `500` | Версионна история на документ |
| `/{id}/published-version` | GET | `READER`, `AUTHOR`, `REVIEWER`, `ADMIN` | няма | `DocumentVersionResponseDto` | `200`, `401`, `403`, `404`, `500` | Връща published версия |
| `/published` | GET | `READER`, `AUTHOR`, `REVIEWER`, `ADMIN` | няма | `List<DocumentResponseDto>` | `200`, `401`, `403`, `500` | Връща документи с published версия |
| `/{id}/published-only` | GET | `READER`, `AUTHOR`, `REVIEWER`, `ADMIN` | няма | `DocumentResponseDto` | `200`, `401`, `403`, `404`, `500` | Връща документ само ако има published версия |

## DocumentVersionController

Base paths: `/documents/{documentId}/versions` и `/api/v1/documents/{documentId}/versions`

| Endpoint | Method | Auth | Request body | Response body | Status codes | Описание |
| --- | --- | --- | --- | --- | --- | --- |
| `/` | POST | `AUTHOR` или `ADMIN` | `DocumentVersionRequestDto` | `DocumentVersionResponseDto` | `201`, `400`, `401`, `403`, `404`, `409`, `500` | Създава нова версия |
| `/` | GET | `AUTHOR`, `REVIEWER`, `ADMIN` | няма | `List<DocumentVersionResponseDto>` | `200`, `401`, `403`, `404`, `500` | Връща всички видими версии |
| `/{versionId}/submit` | POST | `AUTHOR` или `ADMIN` | няма | `DocumentVersionResponseDto` | `200`, `401`, `403`, `404`, `409`, `500` | Изпраща версия за review |

## ApprovalController

Base paths: `/versions` и `/api/v1/versions`

| Endpoint | Method | Auth | Request body | Response body | Status codes | Описание |
| --- | --- | --- | --- | --- | --- | --- |
| `/{id}/approve` | POST | `REVIEWER` или `ADMIN` | няма | `ApprovalResponseDto` | `200`, `401`, `403`, `404`, `409`, `500` | Approve на версия |
| `/{id}/reject` | POST | `REVIEWER` или `ADMIN` | няма | `ApprovalResponseDto` | `200`, `401`, `403`, `404`, `409`, `500` | Reject на версия |

## VersionWorkflowController

Base paths: `/versions` и `/api/v1/versions`

| Endpoint | Method | Auth | Request body | Response body | Status codes | Описание |
| --- | --- | --- | --- | --- | --- | --- |
| `/{versionId}/publish` | POST | `REVIEWER` или `ADMIN` | няма | `PublishDocumentResponseDto` | `200`, `401`, `403`, `404`, `409`, `500` | Публикува approved версия |
| `/{versionId}/rollback` | POST | `REVIEWER` или `ADMIN` | няма | `PublishDocumentResponseDto` | `200`, `401`, `403`, `404`, `409`, `500` | Връща документа към избрана версия |

## DocumentPdfController

Base paths: `/documents` и `/api/v1/documents`

| Endpoint | Method | Auth | Request body | Response body | Status codes | Описание |
| --- | --- | --- | --- | --- | --- | --- |
| `/{id}/published-version/pdf` | GET | `READER`, `AUTHOR`, `REVIEWER`, `ADMIN` | няма | `byte[]` PDF | `200`, `401`, `403`, `404`, `500` | PDF export на published версия |
| `/{documentId}/versions/{versionId}/pdf` | GET | `AUTHOR`, `REVIEWER`, `ADMIN` | няма | `byte[]` PDF | `200`, `401`, `403`, `404`, `500` | PDF export на конкретна версия |

## TestController

Base path: `/test`

| Endpoint | Method | Auth | Request body | Response body | Status codes | Описание |
| --- | --- | --- | --- | --- | --- | --- |
| `/test` | GET | Authenticated by default | няма | `String` | `200`, `401` | Тестов endpoint, връща `Project is running` |

## DTO summary

### Auth

- `RegisterRequestDto` - `username`, `email`, `password`
- `LoginRequestDto` - `username`, `password`
- `AuthResponseDto` - `token`, `username`, `roles`
- `MeResponseDto` - `username`, `email`, `roles`

### Documents

- `DocumentRequestDto` - `title`, `description`
- `UpdateDocumentMetadataRequestDto` - `title`, `description`
- `DocumentResponseDto` - document metadata, owner, status, publishedVersionId, timestamps
- `DocumentHistoryResponseDto` - version history row
- `DocumentVersionRequestDto` - `content`, `message`
- `DocumentVersionResponseDto` - version metadata and content
- `CompareVersionsResponseDto` - two version payloads plus `identical`
- `PublishDocumentResponseDto` - published document summary
- `ApprovalResponseDto` - approval decision summary

## Response shapes

### Error response

Грешките се връщат като `ErrorResponse` с:

- timestamp
- status
- error
- message
- path
- optional validationErrors

### Status code notes

- `201 Created` се използва при създаване на document и version
- `204 No Content` се използва при архивиране, изтриване и role update
- `200 OK` се използва за read операции и workflow резултати

## Не е налично

- endpoint за audit logs
- endpoint за refresh token
- endpoint за logout / revoke token
- endpoint за password reset
- comment payload при approve/reject