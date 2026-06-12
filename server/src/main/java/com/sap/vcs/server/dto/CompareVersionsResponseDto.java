package com.sap.vcs.server.dto;

public class CompareVersionsResponseDto {

    private Integer documentId;
    private Integer leftVersionId;
    private Integer leftVersionNumber;
    private String leftContent;
    private Integer rightVersionId;
    private Integer rightVersionNumber;
    private String rightContent;
    private boolean identical;

    public CompareVersionsResponseDto() {
    }

    public CompareVersionsResponseDto(
            Integer documentId,
            Integer leftVersionId,
            Integer leftVersionNumber,
            String leftContent,
            Integer rightVersionId,
            Integer rightVersionNumber,
            String rightContent,
            boolean identical
    ) {
        this.documentId = documentId;
        this.leftVersionId = leftVersionId;
        this.leftVersionNumber = leftVersionNumber;
        this.leftContent = leftContent;
        this.rightVersionId = rightVersionId;
        this.rightVersionNumber = rightVersionNumber;
        this.rightContent = rightContent;
        this.identical = identical;
    }

    public Integer getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Integer documentId) {
        this.documentId = documentId;
    }

    public Integer getLeftVersionId() {
        return leftVersionId;
    }

    public void setLeftVersionId(Integer leftVersionId) {
        this.leftVersionId = leftVersionId;
    }

    public Integer getLeftVersionNumber() {
        return leftVersionNumber;
    }

    public void setLeftVersionNumber(Integer leftVersionNumber) {
        this.leftVersionNumber = leftVersionNumber;
    }

    public String getLeftContent() {
        return leftContent;
    }

    public void setLeftContent(String leftContent) {
        this.leftContent = leftContent;
    }

    public Integer getRightVersionId() {
        return rightVersionId;
    }

    public void setRightVersionId(Integer rightVersionId) {
        this.rightVersionId = rightVersionId;
    }

    public Integer getRightVersionNumber() {
        return rightVersionNumber;
    }

    public void setRightVersionNumber(Integer rightVersionNumber) {
        this.rightVersionNumber = rightVersionNumber;
    }

    public String getRightContent() {
        return rightContent;
    }

    public void setRightContent(String rightContent) {
        this.rightContent = rightContent;
    }

    public boolean isIdentical() {
        return identical;
    }

    public void setIdentical(boolean identical) {
        this.identical = identical;
    }
}