package com.sap.vcs.server.service;

import com.sap.vcs.server.dto.ApprovalResponseDto;
import com.sap.vcs.server.entity.Approval;
import com.sap.vcs.server.entity.DocumentVersion;
import com.sap.vcs.server.entity.User;
import com.sap.vcs.server.entity.enums.ApprovalDecision;
import com.sap.vcs.server.entity.enums.VersionStatus;
import com.sap.vcs.server.exception.BusinessRuleViolationException;
import com.sap.vcs.server.exception.ResourceNotFoundException;
import com.sap.vcs.server.repository.ApprovalRepository;
import com.sap.vcs.server.repository.DocumentVersionRepository;
import com.sap.vcs.server.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final UserRepository userRepository;

    public ApprovalService(ApprovalRepository approvalRepository,
                           DocumentVersionRepository documentVersionRepository,
                           UserRepository userRepository) {
        this.approvalRepository = approvalRepository;
        this.documentVersionRepository = documentVersionRepository;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
    public ApprovalResponseDto approve(Integer versionId) {
        return processDecision(versionId, ApprovalDecision.APPROVED, VersionStatus.APPROVED);
    }

    @PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
    public ApprovalResponseDto reject(Integer versionId) {
        return processDecision(versionId, ApprovalDecision.REJECTED, VersionStatus.REJECTED);
    }

    private ApprovalResponseDto processDecision(Integer versionId,
                                                ApprovalDecision decision,
                                                VersionStatus targetStatus) {
        DocumentVersion version = documentVersionRepository.findById(versionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Version not found with id: " + versionId));

        if (version.getStatus() != VersionStatus.IN_REVIEW) {
            throw new BusinessRuleViolationException("Only IN_REVIEW versions can be " + decision.name().toLowerCase());
        }

        User reviewer = getCurrentAuthenticatedUser();

        Approval approval = approvalRepository.findByVersionAndReviewer(version, reviewer)
                .orElseGet(Approval::new);

        approval.setVersion(version);
        approval.setReviewer(reviewer);
        approval.setDecision(decision);
        approval.setDecidedAt(LocalDateTime.now());

        version.setStatus(targetStatus);

        Approval savedApproval = approvalRepository.save(approval);
        documentVersionRepository.save(version);

        return mapToResponse(savedApproval);
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalStateException("No authenticated user available in security context");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found with username: " + authentication.getName()
                        ));
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