UPDATE documents
SET created_at = COALESCE(created_at, updated_at, CURRENT_TIMESTAMP)
WHERE created_at IS NULL;

UPDATE documents
SET updated_at = COALESCE(updated_at, created_at, CURRENT_TIMESTAMP)
WHERE updated_at IS NULL;

UPDATE document_versions
SET created_at = CURRENT_TIMESTAMP
WHERE created_at IS NULL;

ALTER TABLE documents
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE document_versions
    ALTER COLUMN created_at SET NOT NULL;
