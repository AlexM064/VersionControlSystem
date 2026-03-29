ALTER TABLE documents
DROP CONSTRAINT chk_documents_status;

ALTER TABLE documents
    ADD CONSTRAINT chk_documents_status
        CHECK (status IN ('DRAFT', 'IN_REVIEW', 'APPROVED', 'REJECTED', 'PUBLISHED', 'ARCHIVED'));