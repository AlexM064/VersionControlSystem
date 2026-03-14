package com.sap.vcs.server.service;

import com.sap.vcs.server.dto.DocumentRequestDto;
import com.sap.vcs.server.dto.DocumentResponseDto;
import com.sap.vcs.server.entity.Document;
import com.sap.vcs.server.exception.ResourceNotFoundException;
import com.sap.vcs.server.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public DocumentResponseDto createDocument(DocumentRequestDto request) {
        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setDescription(request.getDescription());

        Document savedDocument = documentRepository.save(document);
        return mapToResponse(savedDocument);
    }

    public List<DocumentResponseDto> getAllDocuments() {
        return documentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public DocumentResponseDto getDocumentById(Integer id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Document not found with id: " + id));

        return mapToResponse(document);
    }

    private DocumentResponseDto mapToResponse(Document document) {
        return new DocumentResponseDto(
                document.getId(),
                document.getTitle(),
                document.getDescription(),
                document.getStatus() != null ? document.getStatus().name() : null
        );
    }
}