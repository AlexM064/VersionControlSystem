package com.sap.vcs.server.dto;

import java.time.LocalDateTime;

public class DocumentHistoryResponseDto {

    private Integer versionId;
    private Integer versionNumber;
    private String message;
    private LocalDateTime createdAt;
    private boolean published;

    public DocumentHistoryResponseDto() {
    }

    public DocumentHistoryResponseDto(Integer versionId,
                                      Integer versionNumber,
                                      String message,
                                      LocalDateTime createdAt,
                                      boolean published) {
        this.versionId = versionId;
        this.versionNumber = versionNumber;
        this.message = message;
        this.createdAt = createdAt;
        this.published = published;
    }

    public Integer getVersionId() {
        return versionId;
    }

    public void setVersionId(Integer versionId) {
        this.versionId = versionId;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }
}