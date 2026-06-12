INSERT INTO roles (name)
VALUES
    ('ADMIN'),
    ('AUTHOR'),
    ('READER'),
    ('REVIEWER')
ON CONFLICT (name) DO NOTHING;

INSERT INTO users (username, email, password_hash, is_active)
VALUES
    (
        'admin.local',
        'admin.local@sap-vcs.local',
        '$2a$10$cZvpOTNJtaQGG3pBzxXfOeJcWnOYTC21b51ak2dgmN11nSvPOW7US',
        TRUE
    ),
    (
        'author.local',
        'author.local@sap-vcs.local',
        '$2a$10$LEpey3ERXLk3O3rj7TcDD.84yKO2p3Sr1BVEkm66wlfzWCGuMMyoW',
        TRUE
    ),
    (
        'reader.local',
        'reader.local@sap-vcs.local',
        '$2a$10$t6t1wZ5mXXq3oKu3JWyJOO8SFusp56K/wuJHkaNfXwAr9G5oq0U3i',
        TRUE
    ),
    (
        'reviewer.local',
        'reviewer.local@sap-vcs.local',
        '$2a$10$d1N2FxXj.JHfJ981XuJyj.R7CUiMdR/m6YunZT3ar5OQgIUTPloNq',
        TRUE
    ),
    (
        'inactive.local',
        'inactive.local@sap-vcs.local',
        '$2a$10$o6Dsi4KNAdezWd6XBE3Zwetf7GBzWDcpo2XPHqRGxz7PCInhc8hSG',
        FALSE
    )
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ADMIN'
WHERE u.username = 'admin.local'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'AUTHOR'
WHERE u.username = 'author.local'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'READER'
WHERE u.username = 'reader.local'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'REVIEWER'
WHERE u.username = 'reviewer.local'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'AUTHOR'
WHERE u.username = 'inactive.local'
ON CONFLICT (user_id, role_id) DO NOTHING;
