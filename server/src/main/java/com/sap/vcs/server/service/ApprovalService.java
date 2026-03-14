package com.sap.vcs.server.service;

import com.sap.vcs.server.dto.ApprovalResponseDto;
import com.sap.vcs.server.entity.Approval;
import com.sap.vcs.server.entity.DocumentVersion;
import com.sap.vcs.server.entity.User;
import com.sap.vcs.server.entity.enums.ApprovalDecision;
import com.sap.vcs.server.exception.ResourceNotFoundException;
import com.sap.vcs.server.repository.ApprovalRepository;
import com.sap.vcs.server.repository.DocumentVersionRepository;
import com.sap.vcs.server.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final DocumentVersionRepository versionRepository;
    private final UserRepository userRepository;

    public ApprovalService(ApprovalRepository approvalRepository,
                           DocumentVersionRepository versionRepository,
                           UserRepository userRepository) {
        this.approvalRepository = approvalRepository;
        this.versionRepository = versionRepository;
        this.userRepository = userRepository;
    }

    public ApprovalResponseDto approve(Integer versionId, Integer reviewerId) {
        DocumentVersion version = versionRepository.findById(versionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Version not found with id: " + versionId));

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Reviewer not found with id: " + reviewerId));

        Approval approval = approvalRepository.findByVersionAndReviewer(version, reviewer)
                .orElseGet(Approval::new);

        approval.setVersion(version);
        approval.setReviewer(reviewer);
        approval.setDecision(ApprovalDecision.APPROVED);
        approval.setDecidedAt(LocalDateTime.now());

        Approval savedApproval = approvalRepository.save(approval);
        return mapToResponse(savedApproval);
    }

    public ApprovalResponseDto reject(Integer versionId, Integer reviewerId) {
        DocumentVersion version = versionRepository.findById(versionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Version not found with id: " + versionId));

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Reviewer not found with id: " + reviewerId));

        Approval approval = approvalRepository.findByVersionAndReviewer(version, reviewer)
                .orElseGet(Approval::new);

        approval.setVersion(version);
        approval.setReviewer(reviewer);
        approval.setDecision(ApprovalDecision.REJECTED);
        approval.setDecidedAt(LocalDateTime.now());

        Approval savedApproval = approvalRepository.save(approval);
        return mapToResponse(savedApproval);
    }

    private ApprovalResponseDto mapToResponse(Approval approval) {
        return new ApprovalResponseDto(
                approval.getId(),
                approval.getVersion() != null ? approval.getVersion().getId() : null,
                approval.getReviewer() != null ? approval.getReviewer().getId() : null,
                approval.getDecision() != null ? approval.getDecision().name() : null,
                approval.getComment(),
                approval.getDecidedAt()
        );
    }
}