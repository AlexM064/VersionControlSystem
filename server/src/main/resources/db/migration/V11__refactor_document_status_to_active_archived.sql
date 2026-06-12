ALTER TABLE documents DROP CONSTRAINT IF EXISTS chk_documents_status;

UPDATE documents
SET status = 'ACTIVE'
WHERE status IS NULL
   OR status IN ('DRAFT', 'IN_REVIEW', 'APPROVED', 'PUBLISHED', 'REJECTED');

ALTER TABLE documents
    ADD CONSTRAINT chk_documents_status
        CHECK (status IN ('ACTIVE', 'ARCHIVED'));