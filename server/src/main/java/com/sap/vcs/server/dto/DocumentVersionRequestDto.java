package com.sap.vcs.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DocumentVersionRequestDto {

    @NotBlank(message = "Content is required")
    private String content;

    @Size(max = 1000, message = "Message must be at most 1000 characters")
    private String message;

    public DocumentVersionRequestDto() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}