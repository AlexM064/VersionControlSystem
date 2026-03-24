# VersionControlSystem

## Local Authentication And Authorization Testing

### Local Test Accounts

| Username | Password | Role | Status |
| --- | --- | --- | --- |
| `admin.local` | `admin123` | `ADMIN` | Active |
| `author.local` | `author123` | `AUTHOR` | Active |
| `reader.local` | `reader123` | `READER` | Active |
| `reviewer.local` | `reviewer123` | `REVIEWER` | Active |
| `inactive.local` | `inactive123` | `AUTHOR` | Inactive |

These users are seeded by the Flyway migration at `server/src/main/resources/db/migration/V8__seed_local_auth_users.sql`.

### Security Test Coverage

Current security-related tests:

- `server/src/test/java/com/sap/vcs/server/security/SecurityAuthenticationIntegrationTest.java`: proves the real Basic Auth flow works with the configured `PasswordEncoder`, including valid login, wrong password, inactive user, and insufficient-role behavior
- `server/src/test/java/com/sap/vcs/server/controller/DocumentControllerAuthorizationTest.java`: proves controller-level authorization for document endpoints, including allowed role, `403 Forbidden` for a disallowed authenticated role, and `401 Unauthorized` for unauthenticated requests
- `server/src/test/java/com/sap/vcs/server/controller/DocumentVersionControllerAuthorizationTest.java`: proves controller-level authorization for document version endpoints with the same `200` / `403` / `401` coverage pattern
- `server/src/test/java/com/sap/vcs/server/controller/ApprovalControllerAuthorizationTest.java`: proves controller-level authorization for `approve` and `reject` workflow endpoints
- `server/src/test/java/com/sap/vcs/server/controller/VersionWorkflowControllerAuthorizationTest.java`: proves controller-level authorization for `publish` and `rollback` workflow endpoints
- `server/src/test/java/com/sap/vcs/server/service/ServiceMethodSecurityTest.java`: proves the critical `@PreAuthorize` rules at service level for create, approve, reject, publish, and rollback operations. It exists as a second safety net behind controller authorization tests, so business-critical methods stay protected even if a route changes later.

What these tests verify:

- `401 Unauthorized`: authentication failed because credentials are missing, invalid, or the account is inactive
- `403 Forbidden`: authentication succeeded, but that role is not allowed to use the endpoint or service method
- Controller-level authorization tests verify endpoint access rules
- Method-security tests verify `@PreAuthorize` enforcement at service level

### Run Tests Locally

Use the Maven wrapper from the `server` folder.

- Windows PowerShell: `.\mvnw.cmd ...`
- Shells that use `./`: `./mvnw ...`

- Run the full project test suite:
  `.\mvnw.cmd test`
- Run the current security/auth test suite:
  `.\mvnw.cmd "-Dtest=SecurityAuthenticationIntegrationTest,DocumentControllerAuthorizationTest,DocumentVersionControllerAuthorizationTest,ApprovalControllerAuthorizationTest,VersionWorkflowControllerAuthorizationTest,ServiceMethodSecurityTest" test`
- Run only the authentication foundation test:
  `.\mvnw.cmd "-Dtest=SecurityAuthenticationIntegrationTest" test`
- Run only the document controller authorization tests:
  `.\mvnw.cmd "-Dtest=DocumentControllerAuthorizationTest,DocumentVersionControllerAuthorizationTest" test`
- Run only the workflow controller authorization tests:
  `.\mvnw.cmd "-Dtest=ApprovalControllerAuthorizationTest,VersionWorkflowControllerAuthorizationTest" test`
- Run only the service method-security tests:
  `.\mvnw.cmd "-Dtest=ServiceMethodSecurityTest" test`
- Run a single test class while debugging:
  `.\mvnw.cmd "-Dtest=DocumentControllerAuthorizationTest" test`

### Manual Verification With Postman

Use `Authorization -> Basic Auth` in Postman and pick one of the local test accounts above.

Recommended checks:

- Method: `GET`
  Endpoint: `http://localhost:8080/documents`
  Username/password: `author.local / author123`
  Expected status: `200 OK`

- Method: `GET`
  Endpoint: `http://localhost:8080/documents`
  Username/password: `reader.local / reader123`
  Expected status: `403 Forbidden`

- Method: `GET`
  Endpoint: `http://localhost:8080/documents`
  Username/password: `author.local / wrongpass`
  Expected status: `401 Unauthorized`

- Method: `GET`
  Endpoint: `http://localhost:8080/documents`
  Username/password: `inactive.local / inactive123`
  Expected status: `401 Unauthorized`

- Method: `POST`
  Endpoint: `http://localhost:8080/versions/1/publish`
  Username/password: `reviewer.local / reviewer123`
  Expected status: `200 OK` when the target version satisfies the publish business rules

- Method: `POST`
  Endpoint: `http://localhost:8080/versions/1/publish`
  Username/password: `author.local / author123`
  Expected status: `403 Forbidden`

### Final Security Hardening Note

During development, the fallback rule may remain:

`anyRequest().hasRole("ADMIN")`

As a final security-hardening step, this should be changed to:

`anyRequest().denyAll()`

Do this only after:

- the explicit authorization matrix is fully implemented
- authorization tests are in place
- all important endpoints are explicitly covered so no route still depends on the fallback rule
