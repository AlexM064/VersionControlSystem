import { Badge } from './common';
import { DocumentStatus } from '@/types/document';
import { VersionStatus } from '@/types/version';

interface StatusBadgeProps {
  status: DocumentStatus | VersionStatus;
  className?: string;
}

const statusVariantMap: Record<string, 'default' | 'success' | 'warning' | 'error' | 'info'> = {
  [DocumentStatus.ACTIVE]: 'success',
  [DocumentStatus.ARCHIVED]: 'warning',
  DRAFT: 'info',
  IN_REVIEW: 'warning',
  APPROVED: 'success',
  REJECTED: 'error',
  PUBLISHED: 'success',
};

const statusLabelMap: Record<string, string> = {
  [DocumentStatus.ACTIVE]: 'Active',
  [DocumentStatus.ARCHIVED]: 'Archived',
  DRAFT: 'Draft',
  IN_REVIEW: 'In Review',
  APPROVED: 'Approved',
  REJECTED: 'Rejected',
  PUBLISHED: 'Published',
};

export const StatusBadge = ({ status, className = '' }: StatusBadgeProps) => {
  const variant = statusVariantMap[status] || 'default';
  return (
    <Badge variant={variant} className={`min-w-[86px] justify-center ${className}`}>
      {statusLabelMap[status] || status}
    </Badge>
  );
};
