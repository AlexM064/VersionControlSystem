import { Document } from '@/types/document';
import { DocumentVersion, VersionStatus } from '@/types/version';
import { Button } from '@/components/ui';
import { Plus, Send, CheckCircle, XCircle, BookMarked, RotateCcw, Download, GitBranch } from 'lucide-react';

interface DocumentActionsProps {
  document: Document;
  selectedVersion?: DocumentVersion;
  permissions?: {
    canCreateVersion?: boolean;
    canSubmitReview?: boolean;
    canApprove?: boolean;
    canReject?: boolean;
    canPublish?: boolean;
    canRollback?: boolean;
    canDownloadPDF?: boolean;
    canCompareVersions?: boolean;
  };
  onCreateVersion?: () => void;
  onSubmitReview?: (versionId: number) => void;
  onApprove?: (versionId: number) => void;
  onReject?: (versionId: number) => void;
  onPublish?: (versionId: number) => void;
  onRollback?: (versionId: number) => void;
  onDownloadPDF?: () => void;
  onCompareVersions?: () => void;
  isDownloadingPDF?: boolean;
  loadingAction?: 'submit' | 'approve' | 'reject' | 'publish' | 'rollback' | 'create';
}

export const DocumentActions = ({
  document: _document,
  selectedVersion,
  permissions = {
    canCreateVersion: true,
    canSubmitReview: true,
    canApprove: true,
    canReject: true,
    canPublish: true,
    canRollback: true,
    canDownloadPDF: true,
    canCompareVersions: true,
  },
  onCreateVersion,
  onSubmitReview,
  onApprove,
  onReject,
  onPublish,
  onRollback,
  onDownloadPDF,
  onCompareVersions,
  isDownloadingPDF = false,
  loadingAction,
}: DocumentActionsProps) => {
  const isDraft = selectedVersion?.status === VersionStatus.DRAFT;
  const isInReview = selectedVersion?.status === VersionStatus.IN_REVIEW;
  const isApproved = selectedVersion?.status === VersionStatus.APPROVED;
  const isPublished = selectedVersion?.status === VersionStatus.PUBLISHED;
  const hasWorkflowActions =
    !!permissions.canCreateVersion ||
    !!permissions.canSubmitReview ||
    !!permissions.canApprove ||
    !!permissions.canReject ||
    !!permissions.canPublish ||
    !!permissions.canRollback;

  return (
    <div className="bg-white dark:bg-slate-900 rounded-lg border border-slate-200 dark:border-slate-700 p-6 shadow-sm">
      <h3 className="text-lg font-semibold text-slate-900 dark:text-slate-50 mb-4">Actions</h3>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
        {/* Primary Actions */}
        {permissions.canCreateVersion && (
          <Button
            variant="primary"
            size="md"
            onClick={onCreateVersion}
            loading={loadingAction === 'create'}
            className="flex items-center justify-center gap-2"
          >
            <Plus size={18} />
            Create Version
          </Button>
        )}

        {/* Workflow Actions */}
        {isDraft && permissions.canSubmitReview && selectedVersion && (
          <Button
            variant="primary"
            size="md"
            onClick={() => onSubmitReview?.(selectedVersion.id)}
            loading={loadingAction === 'submit'}
            className="flex items-center justify-center gap-2"
          >
            <Send size={18} />
            Submit for Review
          </Button>
        )}

        {isInReview && permissions.canApprove && selectedVersion && (
          <Button
            variant="primary"
            size="md"
            onClick={() => onApprove?.(selectedVersion.id)}
            loading={loadingAction === 'approve'}
            className="flex items-center justify-center gap-2 bg-green-600 hover:bg-green-700"
          >
            <CheckCircle size={18} />
            Approve
          </Button>
        )}

        {isInReview && permissions.canReject && selectedVersion && (
          <Button
            variant="danger"
            size="md"
            onClick={() => onReject?.(selectedVersion.id)}
            loading={loadingAction === 'reject'}
            className="flex items-center justify-center gap-2"
          >
            <XCircle size={18} />
            Reject
          </Button>
        )}

        {isApproved && permissions.canPublish && selectedVersion && (
          <Button
            variant="primary"
            size="md"
            onClick={() => onPublish?.(selectedVersion.id)}
            loading={loadingAction === 'publish'}
            className="flex items-center justify-center gap-2 bg-purple-600 hover:bg-purple-700"
          >
            <BookMarked size={18} />
            Publish
          </Button>
        )}

        {isPublished && permissions.canRollback && selectedVersion && (
          <Button
            variant="danger"
            size="md"
            onClick={() => onRollback?.(selectedVersion.id)}
            loading={loadingAction === 'rollback'}
            className="flex items-center justify-center gap-2"
          >
            <RotateCcw size={18} />
            Rollback
          </Button>
        )}

        {/* Utility Actions */}
        {permissions.canDownloadPDF && (
          <Button
            variant="secondary"
            size="md"
            onClick={onDownloadPDF}
            loading={isDownloadingPDF}
            disabled={!onDownloadPDF || isDownloadingPDF}
            className="flex items-center justify-center gap-2"
            title={!onDownloadPDF ? 'No published version available' : 'Download published version PDF'}
          >
            <Download size={18} />
            Download PDF
          </Button>
        )}

        {permissions.canCompareVersions && (
          <Button
            variant="secondary"
            size="md"
            onClick={onCompareVersions}
            disabled={!onCompareVersions}
            className="flex items-center justify-center gap-2"
            title={!onCompareVersions ? 'Select a version to compare' : 'Compare versions'}
          >
            <GitBranch size={18} />
            Compare Versions
          </Button>
        )}

      </div>

      {!selectedVersion && hasWorkflowActions && (
        <div className="mt-4 p-3 bg-blue-50 border border-blue-200 rounded text-sm text-blue-700">
          💡 Select a version to unlock workflow actions (submit, approve, publish, etc.)
        </div>
      )}

      {!hasWorkflowActions && (
        <div className="mt-4 rounded-lg border border-slate-200 bg-slate-50 p-3 text-sm text-slate-600 dark:border-slate-700 dark:bg-slate-800/50 dark:text-slate-300">
          Your current role has read-only access for workflow actions on this document.
        </div>
      )}
    </div>
  );
};
