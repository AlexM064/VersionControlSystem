INSERT INTO roles (name)
VALUES
    ('ADMIN'),
    ('AUTHOR'),
    ('READER'),
    ('REVIEWER')
ON CONFLICT (name) DO NOTHING;