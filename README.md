# VersionControlSystem

## Local Authentication Testing

### Test Accounts

| Username | Password | Role | Status |
| --- | --- | --- | --- |
| `admin.local` | `admin123` | `ADMIN` | Active |
| `author.local` | `author123` | `AUTHOR` | Active |
| `reader.local` | `reader123` | `READER` | Active |
| `reviewer.local` | `reviewer123` | `REVIEWER` | Active |
| `inactive.local` | `inactive123` | `AUTHOR` | Inactive |

These users are seeded by the Flyway migration at [server/src/main/resources/db/migration/V8__seed_local_auth_users.sql](/d:/Coding%20-%20Samir/VersionControlSAP/VersionControlSystem/server/src/main/resources/db/migration/V8__seed_local_auth_users.sql).

### Expected Behavior

- Inactive user returns `401 Unauthorized`
- Wrong password returns `401 Unauthorized`
- Authenticated user without the required role returns `403 Forbidden`

### 401 vs 403

- `401 Unauthorized`: authentication failed because credentials are missing, invalid, or the account is inactive
- `403 Forbidden`: authentication succeeded, but the user does not have permission for that endpoint

### Postman Testing

Use `Authorization -> Basic Auth` in Postman and enter one of the seeded usernames and passwords above.

Recommended checks:

- Valid login: `GET http://localhost:8080/documents` with `author.local / author123` -> `200 OK`
- Wrong password: `GET http://localhost:8080/documents` with `author.local / wrongpass` -> `401 Unauthorized`
- Inactive user: `GET http://localhost:8080/documents` with `inactive.local / inactive123` -> `401 Unauthorized`
- Insufficient role: `GET http://localhost:8080/documents` with `reader.local / reader123` -> `403 Forbidden`
- Reader access to published version: `GET http://localhost:8080/documents/1/published-version` with `reader.local / reader123` -> `200 OK` when a published document exists
