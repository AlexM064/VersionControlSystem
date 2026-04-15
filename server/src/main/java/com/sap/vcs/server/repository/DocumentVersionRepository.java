package com.sap.vcs.server.repository;

import com.sap.vcs.server.entity.Document;
import com.sap.vcs.server.entity.DocumentVersion;
import com.sap.vcs.server.entity.enums.VersionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Integer> {

    List<DocumentVersion> findByDocumentOrderByVersionNumberAsc(Document document);

    Optional<DocumentVersion> findTopByDocumentOrderByVersionNumberDesc(Document document);

    boolean existsByDocumentAndStatus(Document document, VersionStatus status);

    boolean existsByDocumentAndStatusIn(Document document, Collection<VersionStatus> statuses);
}