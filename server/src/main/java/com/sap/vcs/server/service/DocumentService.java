package com.sap.vcs.server.service;

import com.sap.vcs.server.entity.Document;
import com.sap.vcs.server.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public Document createDocument(Document document) {
        return documentRepository.save(document);
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    public Document getDocumentById(Integer id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
    }
}