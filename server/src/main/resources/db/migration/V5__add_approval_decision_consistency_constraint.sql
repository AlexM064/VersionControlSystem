UPDATE approvals
SET decided_at = NULL
WHERE decision = 'PENDING'
  AND decided_at IS NOT NULL;

UPDATE approvals
SET decided_at = CURRENT_TIMESTAMP
WHERE decision IN ('APPROVED', 'REJECTED')
  AND decided_at IS NULL;

ALTER TABLE approvals
    ADD CONSTRAINT chk_approvals_decision_decided_at_consistency
        CHECK (
            (decision = 'PENDING' AND decided_at IS NULL)
            OR (decision IN ('APPROVED', 'REJECTED') AND decided_at IS NOT NULL)
        );
