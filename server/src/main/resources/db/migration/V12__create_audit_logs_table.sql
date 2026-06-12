CREATE TABLE audit_logs (
                            id SERIAL PRIMARY KEY,
                            action_type VARCHAR(100) NOT NULL,
                            entity_type VARCHAR(50) NOT NULL,
                            entity_id INTEGER NOT NULL,
                            username VARCHAR(50) NOT NULL,
                            details TEXT,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_entity_type_entity_id
    ON audit_logs (entity_type, entity_id);

CREATE INDEX idx_audit_logs_username
    ON audit_logs (username);

CREATE INDEX idx_audit_logs_created_at
    ON audit_logs (created_at);