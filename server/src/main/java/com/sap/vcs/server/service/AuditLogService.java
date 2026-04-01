package com.sap.vcs.server.service;

import com.sap.vcs.server.entity.AuditLog;
import com.sap.vcs.server.entity.enums.AuditActionType;
import com.sap.vcs.server.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(AuditActionType actionType,
                    String entityType,
                    Integer entityId,
                    String username,
                    String details) {

        AuditLog auditLog = new AuditLog();
        auditLog.setActionType(actionType);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setUsername(username);
        auditLog.setDetails(details);

        auditLogRepository.save(auditLog);
    }
}