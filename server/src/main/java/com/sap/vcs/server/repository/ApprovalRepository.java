package com.sap.vcs.server.repository;

import com.sap.vcs.server.entity.Approval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Integer> {
}

