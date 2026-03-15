package com.sap.vcs.server.dto;

public class PublishDocumentResponseDto {

    private Integer documentId;
    private String title;
    private String status;
    private Integer publishedVersionId;
    private Integer publishedVersionNumber;

    public PublishDocumentResponseDto() {
    }

    public PublishDocumentResponseDto(
            Integer documentId,
            String title,
            String status,
            Integer publishedVersionId,
            Integer publishedVersionNumber
    ) {
        this.documentId = documentId;
        this.title = title;
        this.status = status;
        this.publishedVersionId = publishedVersionId;
        this.publishedVersionNumber = publishedVersionNumber;
    }

    public Integer getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Integer documentId) {
        this.documentId = documentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPublishedVersionId() {
        return publishedVersionId;
    }

    public void setPublishedVersionId(Integer publishedVersionId) {
        this.publishedVersionId = publishedVersionId;
    }

    public Integer getPublishedVersionNumber() {
        return publishedVersionNumber;
    }

    public void setPublishedVersionNumber(Integer publishedVersionNumber) {
        this.publishedVersionNumber = publishedVersionNumber;
    }
}