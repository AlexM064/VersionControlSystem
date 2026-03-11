package com.sap.vcs.server.entity;

import com.sap.vcs.server.entity.enums.ApprovalDecision;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "approvals",
       uniqueConstraints = @UniqueConstraint(columnNames = {"version_id", "reviewer_id"}))
public class Approval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "version_id")
    private DocumentVersion version;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalDecision decision = ApprovalDecision.PENDING;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    public Approval() {
    }

    // getters & setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DocumentVersion getVersion() {
        return version;
    }

    public void setVersion(DocumentVersion version) {
        this.version = version;
    }

    public User getReviewer() {
        return reviewer;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

    public ApprovalDecision getDecision() {
        return decision;
    }

    public void setDecision(ApprovalDecision decision) {
        this.decision = decision;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(LocalDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }
}
