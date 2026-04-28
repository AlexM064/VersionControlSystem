# База данни и домейн модел

## Основни entity класове

### User

Поля:

- `id`
- `username`
- `email`
- `passwordHash`
- `isActive`
- `createdAt`
- `roles`

### Role

Поля:

- `id`
- `name`

### Document

Поля:

- `id`
- `title`
- `description`
- `owner`
- `status`
- `createdAt`
- `updatedAt`
- `versions`
- `publishedVersion`

### DocumentVersion

Поля:

- `id`
- `document`
- `versionNumber`
- `status`
- `content`
- `message`
- `createdBy`
- `createdAt`
- `approvals`

### Approval

Поля:

- `id`
- `version`
- `reviewer`
- `decision`
- `comment`
- `decidedAt`

### AuditLog

Поля:

- `id`
- `actionType`
- `entityType`
- `entityId`
- `username`
- `details`
- `createdAt`

## Таблици и връзки

### `users`

- уникален `username`
- уникален `email`
- `password_hash`
- `is_active`
- `created_at`

### `roles`

- уникално `name`

### `user_roles`

- join table между `users` и `roles`
- primary key върху `(user_id, role_id)`

### `documents`

- принадлежи на `owner_id`
- има `status`
- пази `published_version_id`

### `document_versions`

- принадлежи на `document_id`
- има уникалност на `(document_id, version_number)`
- пази `created_by`

### `approvals`

- принадлежи на `version_id`
- принадлежи на `reviewer_id`
- уникалност на `(version_id, reviewer_id)`

### `audit_logs`

- самостоятелна таблица за проследимост на действията

## Роли на потребителите

Системата използва четири роли:

- `ADMIN`
- `AUTHOR`
- `REVIEWER`
- `READER`

Те се записват в таблицата `roles` и се свързват с потребителите чрез `user_roles`.

## Document / Version / Approval workflow

### Document

Документът е основен контейнер за съдържание и версии. В кода е видим през `Document` entity и `DocumentService`.

### Version

Версиите са отделни записи в `document_versions`. Всяка версия има:

- номер, увеличаващ се последователно за даден документ
- content и message
- статус `DRAFT`, `IN_REVIEW`, `APPROVED`, `REJECTED` или `PUBLISHED`

### Approval

Approval записите съхраняват решението на reviewer за конкретна версия. Решението е `PENDING`, `APPROVED` или `REJECTED`.

### Publish / rollback

- publish е позволен само за `APPROVED` версии
- rollback е позволен за `APPROVED` или `PUBLISHED` версии
- publish актуализира `published_version_id` на документа и го маркира като `ACTIVE`

## ER модел в текстов вид

```text
User ---< user_roles >--- Role
User 1 --- N Document (owner)
Document 1 --- N DocumentVersion
User 1 --- N DocumentVersion (createdBy)
DocumentVersion 1 --- N Approval
User 1 --- N Approval (reviewer)
Document 1 --- 0..1 DocumentVersion (publishedVersion)
AuditLog is independent
```

## Как са използвани status полетата

### DocumentStatus

- `DRAFT`
- `ACTIVE`
- `ARCHIVED`

### VersionStatus

- `DRAFT`
- `IN_REVIEW`
- `APPROVED`
- `REJECTED`
- `PUBLISHED`

### ApprovalDecision

- `PENDING`
- `APPROVED`
- `REJECTED`

### AuditActionType

- `DOCUMENT_CREATED`
- `DOCUMENT_UPDATED`
- `DOCUMENT_ARCHIVED`
- `DOCUMENT_DELETED`
- `VERSION_CREATED`
- `VERSION_SUBMITTED_FOR_REVIEW`
- `VERSION_APPROVED`
- `VERSION_REJECTED`
- `VERSION_PUBLISHED`
- `VERSION_ROLLED_BACK`

## Flyway migrations

Ключовите миграции са:

- `V1__Initial_setup.sql` - основните таблици
- `V2__add_published_version_to_documents.sql` - published version FK
- `V8__seed_local_auth_users.sql` - локални test accounts
- `V9__ensure_roles_exist.sql` - seed на ролите
- `V10__add_status_to_document_versions.sql` - version status
- `V11__refactor_document_status_to_active_archived.sql` - нормализация на document status
- `V12__create_audit_logs_table.sql` - audit logs table
- `V14__align_documents_status_constraint_with_document_lifecycle.sql` - final status constraint

## Локални test accounts

Миграцията `V8__seed_local_auth_users.sql` създава следните потребители:

- `admin.local` / `admin123` - `ADMIN`
- `author.local` / `author123` - `AUTHOR`
- `reader.local` / `reader123` - `READER`
- `reviewer.local` / `reviewer123` - `REVIEWER`
- `inactive.local` / `inactive123` - `AUTHOR`, но неактивен

## Важни бележки

- Няма отделна таблица за refresh tokens
- Няма таблица за attachments или binary upload
- Няма API за audit log browsing, въпреки че таблицата съществува