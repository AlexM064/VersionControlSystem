package com.sap.vcs.server.dto;

import com.sap.vcs.server.entity.enums.VersionStatus;

import java.time.LocalDateTime;

public class DocumentVersionResponseDto {

    private Integer id;
    private Integer documentId;
    private Integer versionNumber;
    private String content;
    private String message;
    private VersionStatus status;
    private String createdByUsername;
    private LocalDateTime createdAt;

    public DocumentVersionResponseDto() {
    }

    public DocumentVersionResponseDto(
            Integer id,
            Integer documentId,
            Integer versionNumber,
            String content,
            String message,
            VersionStatus status,
            String createdByUsername,
            LocalDateTime createdAt) {
        this.id = id;
        this.documentId = documentId;
        this.versionNumber = versionNumber;
        this.content = content;
        this.message = message;
        this.status = status;
        this.createdByUsername = createdByUsername;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public Integer getDocumentId() {
        return documentId;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public String getContent() {
        return content;
    }

    public String getMessage() {
        return message;
    }

    public VersionStatus getStatus() {
        return status;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setDocumentId(Integer documentId) {
        this.documentId = documentId;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatus(VersionStatus status) {
        this.status = status;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}