package com.sap.vcs.server.dto;

import java.time.LocalDateTime;

public class ApprovalResponseDto {

    private Integer id;
    private Integer versionId;
    private Integer reviewerId;
    private String decision;
    private String comment;
    private LocalDateTime decidedAt;

    public ApprovalResponseDto() {
    }

    public ApprovalResponseDto(
            Integer id,
            Integer versionId,
            Integer reviewerId,
            String decision,
            String comment,
            LocalDateTime decidedAt) {
        this.id = id;
        this.versionId = versionId;
        this.reviewerId = reviewerId;
        this.decision = decision;
        this.comment = comment;
        this.decidedAt = decidedAt;
    }

    public Integer getId() {
        return id;
    }

    public Integer getVersionId() {
        return versionId;
    }

    public Integer getReviewerId() {
        return reviewerId;
    }

    public String getDecision() {
        return decision;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setVersionId(Integer versionId) {
        this.versionId = versionId;
    }

    public void setReviewerId(Integer reviewerId) {
        this.reviewerId = reviewerId;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setDecidedAt(LocalDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }
}