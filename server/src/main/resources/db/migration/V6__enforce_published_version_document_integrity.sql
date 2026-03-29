UPDATE documents d
SET published_version_id = NULL
WHERE published_version_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM document_versions dv
      WHERE dv.id = d.published_version_id
        AND dv.document_id = d.id
  );

ALTER TABLE documents
    DROP CONSTRAINT fk_documents_published_version;

ALTER TABLE document_versions
    ADD CONSTRAINT uq_document_versions_id_document_id
        UNIQUE (id, document_id);

ALTER TABLE documents
    ADD CONSTRAINT fk_documents_published_version_same_document
        FOREIGN KEY (published_version_id, id)
            REFERENCES document_versions (id, document_id);
