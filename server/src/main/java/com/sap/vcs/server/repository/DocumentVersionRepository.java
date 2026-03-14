package com.sap.vcs.server.repository;

import com.sap.vcs.server.entity.Document;
import com.sap.vcs.server.entity.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentVersionRepository
        extends JpaRepository<DocumentVersion, Integer> {

    List<DocumentVersion> findByDocument(Document document);
    Integer countByDocument(Document document);
}