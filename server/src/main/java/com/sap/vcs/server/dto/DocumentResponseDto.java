package com.sap.vcs.server.dto;

public class DocumentResponseDto {

    private Integer id;
    private String title;
    private String description;
    private String status;
    private Integer publishedVersionId;

    public DocumentResponseDto() {
    }

    public DocumentResponseDto(
            Integer id,
            String title,
            String description,
            String status,
            Integer publishedVersionId
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.publishedVersionId = publishedVersionId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}