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
  [VersionStatus.DRAFT]: 'info',
  [VersionStatus.IN_REVIEW]: 'warning',
  [VersionStatus.APPROVED]: 'success',
  [VersionStatus.REJECTED]: 'error',
  [VersionStatus.PUBLISHED]: 'success',
};

const statusLabelMap: Record<string, string> = {
  [DocumentStatus.ACTIVE]: 'Active',
  [DocumentStatus.ARCHIVED]: 'Archived',
  [VersionStatus.DRAFT]: 'Draft',
  [VersionStatus.IN_REVIEW]: 'In Review',
  [VersionStatus.APPROVED]: 'Approved',
  [VersionStatus.REJECTED]: 'Rejected',
  [VersionStatus.PUBLISHED]: 'Published',
};

export const StatusBadge = ({ status, className = '' }: StatusBadgeProps) => {
  const variant = statusVariantMap[status] || 'default';
  return (
    <Badge variant={variant} className={`min-w-[86px] justify-center ${className}`}>
      {statusLabelMap[status] || status}
    </Badge>
  );
};
