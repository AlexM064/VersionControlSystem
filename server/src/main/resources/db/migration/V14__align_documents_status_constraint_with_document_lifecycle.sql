ALTER TABLE documents
    DROP CONSTRAINT IF EXISTS chk_documents_status;

-- Normalize legacy document-level statuses that actually belong to versions.
UPDATE documents
SET status = CASE
    WHEN status = 'PUBLISHED' THEN 'ACTIVE'
    WHEN status IN ('IN_REVIEW', 'APPROVED', 'REJECTED') THEN 'DRAFT'
    WHEN status IS NULL THEN 'DRAFT'
    ELSE status
END;

ALTER TABLE documents
    ALTER COLUMN status SET DEFAULT 'DRAFT';

ALTER TABLE documents
    ADD CONSTRAINT chk_documents_status
        CHECK (status IN ('DRAFT', 'ACTIVE', 'ARCHIVED'));
