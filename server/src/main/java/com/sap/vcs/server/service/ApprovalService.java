package com.sap.vcs.server.service;

import com.sap.vcs.server.entity.Approval;
import com.sap.vcs.server.entity.DocumentVersion;
import com.sap.vcs.server.entity.User;
import com.sap.vcs.server.entity.enums.ApprovalDecision;
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

    public Approval approve(Integer versionId, Integer reviewerId) {
        DocumentVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Version not found"));

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        Approval approval = approvalRepository.findByVersionAndReviewer(version, reviewer)
                .orElseGet(Approval::new);

        approval.setVersion(version);
        approval.setReviewer(reviewer);
        approval.setDecision(ApprovalDecision.APPROVED);
        approval.setDecidedAt(LocalDateTime.now());

        return approvalRepository.save(approval);
    }

    public Approval reject(Integer versionId, Integer reviewerId) {
        DocumentVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Version not found"));

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        Approval approval = approvalRepository.findByVersionAndReviewer(version, reviewer)
                .orElseGet(Approval::new);

        approval.setVersion(version);
        approval.setReviewer(reviewer);
        approval.setDecision(ApprovalDecision.REJECTED);
        approval.setDecidedAt(LocalDateTime.now());

        return approvalRepository.save(approval);
    }
}
