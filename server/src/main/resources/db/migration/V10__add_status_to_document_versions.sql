ALTER TABLE document_versions
    ADD COLUMN status VARCHAR(20);

UPDATE document_versions dv
SET status = CASE
     WHEN dv.id = d.published_version_id THEN 'PUBLISHED'
     WHEN d.status = 'IN_REVIEW'
         AND dv.version_number = (
             SELECT MAX(dv2.version_number)
             FROM document_versions dv2
             WHERE dv2.document_id = dv.document_id
         ) THEN 'IN_REVIEW'
     WHEN d.status = 'APPROVED'
         AND dv.version_number = (
             SELECT MAX(dv2.version_number)
             FROM document_versions dv2
             WHERE dv2.document_id = dv.document_id
         ) THEN 'APPROVED'
     WHEN d.status = 'REJECTED'
         AND dv.version_number = (
             SELECT MAX(dv2.version_number)
             FROM document_versions dv2
             WHERE dv2.document_id = dv.document_id
         ) THEN 'REJECTED'
     ELSE 'DRAFT'
    END
    FROM documents d
WHERE d.id = dv.document_id;

ALTER TABLE document_versions
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE document_versions
    ADD CONSTRAINT chk_document_versions_status
        CHECK (status IN ('DRAFT', 'IN_REVIEW', 'APPROVED', 'REJECTED', 'PUBLISHED'));

CREATE INDEX idx_document_versions_status ON document_versions(status);