package com.sap.vcs.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DocumentVersionRequestDto {

    @NotBlank(message = "Content is required")
    @Size(min = 1, max = 50000, message = "Content must be between 1 and 50000 characters")
    private String content;

    @Size(max = 1000, message = "Message must be at most 1000 characters")
    private String message;

    public DocumentVersionRequestDto() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message == null ? null : message.trim();
    }
}