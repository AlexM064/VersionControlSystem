CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE user_roles (
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id INT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

CREATE TABLE documents (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    owner_id INT REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_documents_status
    CHECK (status IN ('DRAFT', 'IN_REVIEW', 'APPROVED', 'REJECTED', 'ARCHIVED'))
);

CREATE INDEX idx_documents_owner_id ON documents(owner_id);

CREATE TABLE document_versions (
    id SERIAL PRIMARY KEY,
    document_id INT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    version_number INT NOT NULL,
    content TEXT,
    message TEXT,
    created_by INT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_document_versions_doc_ver UNIQUE (document_id, version_number),
    CONSTRAINT chk_version_number_positive CHECK (version_number > 0)
);

CREATE INDEX idx_document_versions_document_id ON document_versions(document_id);
CREATE INDEX idx_document_versions_created_by ON document_versions(created_by);

CREATE TABLE approvals (
    id SERIAL PRIMARY KEY,
    version_id INT NOT NULL REFERENCES document_versions(id) ON DELETE CASCADE,
    reviewer_id INT NOT NULL REFERENCES users(id),
    decision VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    comment TEXT,
    decided_at TIMESTAMP,
    CONSTRAINT chk_approvals_decision
    CHECK (decision IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT uq_approvals_version_reviewer UNIQUE (version_id, reviewer_id)
);

CREATE INDEX idx_approvals_version_id ON approvals(version_id);
CREATE INDEX idx_approvals_reviewer_id ON approvals(reviewer_id);