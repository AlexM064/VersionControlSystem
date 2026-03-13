package com.sap.vcs.server.repository;

import com.sap.vcs.server.entity.Approval;
import com.sap.vcs.server.entity.DocumentVersion;
import com.sap.vcs.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApprovalRepository extends JpaRepository<Approval, Integer> {
    Optional<Approval> findByVersionAndReviewer(DocumentVersion version, User reviewer);
}
