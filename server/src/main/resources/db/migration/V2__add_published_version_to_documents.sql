ALTER TABLE documents
    ADD COLUMN published_version_id INT;

ALTER TABLE documents
    ADD CONSTRAINT fk_documents_published_version
        FOREIGN KEY (published_version_id)
            REFERENCES document_versions(id);