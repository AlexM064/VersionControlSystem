package com.sap.vcs.server.service;

import com.sap.vcs.server.dto.ApprovalResponseDto;
import com.sap.vcs.server.entity.Approval;
import com.sap.vcs.server.entity.DocumentVersion;
import com.sap.vcs.server.entity.User;
import com.sap.vcs.server.entity.enums.ApprovalDecision;
import com.sap.vcs.server.entity.enums.AuditActionType;
import com.sap.vcs.server.entity.enums.VersionStatus;
import com.sap.vcs.server.exception.BusinessRuleViolationException;
import com.sap.vcs.server.exception.ResourceNotFoundException;
import com.sap.vcs.server.repository.ApprovalRepository;
import com.sap.vcs.server.repository.DocumentVersionRepository;
import com.sap.vcs.server.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final AuditLogService auditLogService;
    private final AuthenticatedUserService authenticatedUserService;

    public ApprovalService(ApprovalRepository approvalRepository,
                           DocumentVersionRepository documentVersionRepository,
                           UserRepository userRepository,
                           AuditLogService auditLogService,
                           AuthenticatedUserService authenticatedUserService) {
        this.approvalRepository = approvalRepository;
        this.documentVersionRepository = documentVersionRepository;
        this.auditLogService = auditLogService;
        this.authenticatedUserService = authenticatedUserService;
    }

    private ApprovalResponseDto processDecision(
            Integer versionId,
            ApprovalDecision decision,
            VersionStatus targetStatus,
            AuditActionType auditActionType,
            String actionVerb
    ) {
        DocumentVersion version = documentVersionRepository.findById(versionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Version not found with id: " + versionId));

        User reviewer = authenticatedUserService.getCurrentUser();

        Approval approval = approvalRepository.findByVersionAndReviewer(version, reviewer)
                .orElseGet(Approval::new);

        if (version.getStatus() != VersionStatus.IN_REVIEW) {
            throw new BusinessRuleViolationException(
                    "Only IN_REVIEW versions can be " + actionVerb.toLowerCase()
            );
        }

        approval.setVersion(version);
        approval.setReviewer(reviewer);
        approval.setDecision(decision);
        approval.setDecidedAt(LocalDateTime.now());

        version.setStatus(targetStatus);

        Approval savedApproval = approvalRepository.save(approval);
        documentVersionRepository.save(version);

        auditLogService.log(
                auditActionType,
                "DOCUMENT_VERSION",
                version.getId(),
                reviewer.getUsername(),
                actionVerb + " version " + version.getVersionNumber() +
                        " for document " + version.getDocument().getId()
        );

        return mapToResponse(savedApproval);
    }

    @PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
    public ApprovalResponseDto approve(Integer versionId) {
        return processDecision(
                versionId,
                ApprovalDecision.APPROVED,
                VersionStatus.APPROVED,
                AuditActionType.VERSION_APPROVED,
                "Approved"
        );
    }

    @PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
    public ApprovalResponseDto reject(Integer versionId) {
        return processDecision(
                versionId,
                ApprovalDecision.REJECTED,
                VersionStatus.REJECTED,
                AuditActionType.VERSION_REJECTED,
                "Rejected"
        );
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