import { useEffect, useRef, useState } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import { useDocumentDetails, useDocumentVersions, usePublishedVersion } from '@/hooks/useDocumentDetails';
import {
  usePermission,
  useSubmitVersion,
  useApproveVersion,
  useRejectVersion,
  usePublishVersion,
  useRollbackVersion,
} from '@/hooks';
import { useAuth } from '@/contexts/AuthContext';
import { DocumentMetadataCard } from '@/components/documents/DocumentMetadataCard';
import { PublishedVersionSection } from '@/components/documents/PublishedVersionSection';
import { VersionsTable } from '@/components/documents/VersionsTable';
import { DocumentActions } from '@/components/documents/DocumentActions';
import { Alert, SectionCard, Skeleton } from '@/components/ui';
import { DocumentVersion } from '@/types/version';
import { UserRole } from '@/types/auth';
import { pdfApi, versionsApi } from '@/api';
import { downloadBlob, sanitizeFilename } from '@/utils/download';
import { useToast } from '@/contexts/ToastContext';

type ConfirmationAction = {
  title: string;
  message: string;
  confirmLabel: string;
  confirmVariant: 'primary' | 'danger';
  run: () => Promise<unknown>;
};

export const DocumentDetailsPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { can } = usePermission();
  const { user } = useAuth();
  const toast = useToast();

  const documentId = parseInt(id || '0', 10);
  const isAdmin = user?.roles.includes(UserRole.ADMIN);
  const isAuthor = user?.roles.includes(UserRole.AUTHOR);
  const isReviewer = user?.roles.includes(UserRole.REVIEWER);
  const isReaderOnly = !isAdmin && !isAuthor && !isReviewer && user?.roles.includes(UserRole.READER);
  const [page, setPage] = useState(1);
  const [selectedVersion, setSelectedVersion] = useState<DocumentVersion | undefined>();
  const [workflowError, setWorkflowError] = useState('');
  const [confirmationAction, setConfirmationAction] = useState<ConfirmationAction | null>(null);
  const [downloadingPublishedPdf, setDownloadingPublishedPdf] = useState(false);
  const [downloadingVersionId, setDownloadingVersionId] = useState<number | null>(null);
  const hasAppliedQueryVersion = useRef(false);

  const submitMutation = useSubmitVersion(documentId);
  const approveMutation = useApproveVersion(documentId);
  const rejectMutation = useRejectVersion(documentId);
  const publishMutation = usePublishVersion(documentId);
  const rollbackMutation = useRollbackVersion(documentId);

  // Load document data
  const {
    data: document,
    isLoading: isLoadingDocument,
    error: documentError,
  } = useDocumentDetails(documentId, isReaderOnly);

  // Load versions
  const {
    data: versionsData,
    isLoading: isLoadingVersions,
    error: versionsError,
  } = useDocumentVersions(documentId, page, 10, !isReaderOnly);

  // Load published version
  const {
    data: publishedVersion,
    isLoading: isLoadingPublished,
  } = usePublishedVersion(documentId, document?.publishedVersionId != null);

  const requestedVersionId = Number(searchParams.get('versionId') || 0);
  const versions = versionsData?.content || [];
  const totalPages = versionsData?.totalPages || 1;

  useEffect(() => {
    hasAppliedQueryVersion.current = false;
  }, [documentId, requestedVersionId]);

  useEffect(() => {
    if (hasAppliedQueryVersion.current) return;
    if (!requestedVersionId || !documentId || isReaderOnly || isLoadingVersions) return;

    const matchingVersion = versions.find((version) => version.id === requestedVersionId);

    if (matchingVersion) {
      setSelectedVersion(matchingVersion);
      hasAppliedQueryVersion.current = true;
      return;
    }

    const fetchRequestedVersion = async () => {
      try {
        const requestedVersion = await versionsApi.getById(documentId, requestedVersionId);
        setSelectedVersion(requestedVersion);
      } catch {
        // Graceful fallback: keep current/default selection behavior.
      } finally {
        hasAppliedQueryVersion.current = true;
      }
    };

    fetchRequestedVersion();
  }, [requestedVersionId, documentId, isReaderOnly, isLoadingVersions, versions]);

  const documentTitle = document?.title ?? 'document';

  const clearSelectedVersion = () => {
    setSelectedVersion(undefined);
  };

  const handleDownloadPublishedPdf = async () => {
    if (!publishedVersion) return;

    try {
      setDownloadingPublishedPdf(true);
      const blob = await pdfApi.getPublishedVersionPdf(documentId);
      downloadBlob(blob, `${sanitizeFilename(documentTitle)}-published-v${publishedVersion.versionNumber}.pdf`);
      toast.success('Published PDF downloaded.');
    } catch (error: any) {
      const message = error?.message || 'Failed to download published version PDF';
      setWorkflowError(message);
      toast.error(message, 'Download failed');
    } finally {
      setDownloadingPublishedPdf(false);
    }
  };

  const handleDownloadVersionPdf = async (version: DocumentVersion) => {
    try {
      setDownloadingVersionId(version.id);
      const blob = await pdfApi.getVersionPdf(documentId, version.id);
      downloadBlob(blob, `${sanitizeFilename(documentTitle)}-v${version.versionNumber}.pdf`);
      toast.success(`Version v${version.versionNumber} downloaded.`);
    } catch (error: any) {
      const message = error?.message || 'Failed to download version PDF';
      setWorkflowError(message);
      toast.error(message, 'Download failed');
    } finally {
      setDownloadingVersionId(null);
    }
  };

  const runWorkflowAction = async (action: () => Promise<unknown>, successMessage: string) => {
    try {
      setWorkflowError('');
      await action();
      clearSelectedVersion();
      toast.success(successMessage);
    } catch (error: any) {
      const message = error?.response?.data?.message || error?.message || 'An error occurred';
      setWorkflowError(message);
      toast.error(message, 'Action failed');
    }
  };

  const openConfirmation = (action: ConfirmationAction) => {
    setWorkflowError('');
    setConfirmationAction(action);
  };

  const closeConfirmation = () => {
    setConfirmationAction(null);
  };

  const selectedVersionId = selectedVersion?.id;

  if (!documentId || isNaN(documentId)) {
    return (
      <div className="space-y-4">
        <Alert
          type="error"
          title="Invalid Document"
          message="Document ID is missing or invalid"
          dismissible={false}
        />
      </div>
    );
  }

  if (isLoadingDocument) {
    return (
      <div className="space-y-6">
        <SectionCard>
          <div className="space-y-3 p-6">
            <Skeleton className="h-7 w-1/3" />
            <Skeleton className="h-4 w-1/2" />
            <Skeleton className="h-20 w-full" />
          </div>
        </SectionCard>
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
          <SectionCard className="lg:col-span-2">
            <div className="space-y-3 p-6">
              <Skeleton className="h-6 w-48" />
              <Skeleton className="h-40 w-full" />
            </div>
          </SectionCard>
          <SectionCard>
            <div className="space-y-3 p-6">
              <Skeleton className="h-6 w-24" />
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
            </div>
          </SectionCard>
        </div>
      </div>
    );
  }

  if (documentError || !document) {
    return (
      <div className="space-y-4">
        <Alert
          type="error"
          title="Failed to load document"
          message={documentError instanceof Error ? documentError.message : 'Document not found'}
          dismissible={false}
        />
      </div>
    );
  }

  const loadingAction =
    submitMutation.isPending
      ? 'submit'
      : approveMutation.isPending
        ? 'approve'
        : rejectMutation.isPending
          ? 'reject'
          : publishMutation.isPending
            ? 'publish'
            : rollbackMutation.isPending
              ? 'rollback'
              : undefined;

  return (
    <div className="space-y-6">
      {workflowError && (
        <Alert
          type="error"
          title="Action failed"
          message={workflowError}
          onClose={() => setWorkflowError('')}
        />
      )}

      {/* Document Metadata */}
      <DocumentMetadataCard document={document} />

      {versionsError && (
        <Alert
          type="error"
          title="Failed to load versions"
          message={versionsError instanceof Error ? versionsError.message : 'Could not load document versions'}
          dismissible={false}
        />
      )}

      {/* Two Column Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Content - Versions */}
        <div className="lg:col-span-2 space-y-6">
          {/* Published Version */}
          <PublishedVersionSection
            publishedVersion={publishedVersion}
            isLoading={isLoadingPublished}
            onDownloadPdf={publishedVersion ? handleDownloadPublishedPdf : undefined}
            isDownloadingPdf={downloadingPublishedPdf}
          />

          {!isReaderOnly && (
            <div>
                  <h2 className="text-xl font-semibold text-slate-900 dark:text-slate-50 mb-4">Version History</h2>
              <VersionsTable
                versions={versions}
                isLoading={isLoadingVersions}
                currentPage={page}
                totalPages={totalPages}
                onPageChange={setPage}
                onDownloadPdf={handleDownloadVersionPdf}
                downloadingVersionId={downloadingVersionId}
                onViewVersion={(version) => {
                  setSelectedVersion(version);
                  // TODO: Navigate to version details view
                  console.log('View version:', version.id);
                }}
                onCompare={(version) => {
                  const fallbackVersionId =
                    publishedVersion?.id ?? versions.find((candidate) => candidate.id !== version.id)?.id ?? version.id;
                  navigate(
                    `/documents/${documentId}/compare?leftVersionId=${fallbackVersionId}&rightVersionId=${version.id}`
                  );
                }}
              />
            </div>
          )}
        </div>

        {/* Sidebar - Actions */}
        <div className="lg:col-span-1">
          <DocumentActions
            document={document}
            selectedVersion={selectedVersion}
            permissions={{
              canCreateVersion: !isReaderOnly && can('CREATE_VERSION'),
              canSubmitReview: !isReaderOnly && can('CREATE_VERSION'), // Assuming submit requires create permission
              canApprove: !isReaderOnly && can('APPROVE_VERSION'),
              canReject: !isReaderOnly && can('REJECT_VERSION'),
              canPublish: !isReaderOnly && can('PUBLISH_VERSION'),
              canRollback: !isReaderOnly && can('ROLLBACK_VERSION'),
              canDownloadPDF: true,
              canCompareVersions: !isReaderOnly,
            }}
            isDownloadingPDF={downloadingPublishedPdf}
            loadingAction={loadingAction}
            onCreateVersion={!isReaderOnly ? () => navigate(`/documents/${documentId}/versions/create`) : undefined}
            onSubmitReview={!isReaderOnly ? (versionId) => {
              runWorkflowAction(() => submitMutation.mutateAsync(versionId), 'Version submitted for review.');
            } : undefined}
            onApprove={!isReaderOnly ? (versionId) => {
              runWorkflowAction(() => approveMutation.mutateAsync(versionId), 'Version approved.');
            } : undefined}
            onReject={!isReaderOnly ? (versionId) => {
              openConfirmation({
                title: 'Reject Version',
                message: 'Are you sure you want to reject this version? This action can affect the review workflow.',
                confirmLabel: 'Reject',
                confirmVariant: 'danger',
                run: () => runWorkflowAction(() => rejectMutation.mutateAsync(versionId), 'Version rejected.'),
              });
            } : undefined}
            onPublish={!isReaderOnly ? (versionId) => {
              openConfirmation({
                title: 'Publish Version',
                message: 'Are you sure you want to publish this version? The published version will become visible as the active document version.',
                confirmLabel: 'Publish',
                confirmVariant: 'primary',
                run: () => runWorkflowAction(() => publishMutation.mutateAsync(versionId), 'Version published successfully.'),
              });
            } : undefined}
            onRollback={!isReaderOnly ? (versionId) => {
              openConfirmation({
                title: 'Rollback Version',
                message: 'Are you sure you want to rollback to this version? This will change the document back to the selected version.',
                confirmLabel: 'Rollback',
                confirmVariant: 'danger',
                run: () => runWorkflowAction(() => rollbackMutation.mutateAsync(versionId), 'Rollback completed.'),
              });
            } : undefined}
            onDownloadPDF={publishedVersion ? handleDownloadPublishedPdf : undefined}
            onCompareVersions={!isReaderOnly ? () => navigate(`/documents/${documentId}/compare`) : undefined}
          />
        </div>
      </div>

      {confirmationAction && selectedVersionId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4">
          <div className="w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
            <h3 className="text-lg font-semibold text-slate-900 dark:text-slate-50">{confirmationAction.title}</h3>
            <p className="mt-3 text-sm text-slate-600 dark:text-slate-400">{confirmationAction.message}</p>

            <div className="mt-6 flex justify-end gap-3">
              <button
                type="button"
                className="rounded-lg border border-slate-300 dark:border-slate-600 px-4 py-2 text-sm font-medium text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors duration-150 disabled:opacity-50"
                onClick={closeConfirmation}
                disabled={rejectMutation.isPending || publishMutation.isPending || rollbackMutation.isPending}
              >
                Cancel
              </button>
              <button
                type="button"
                className={`rounded-lg px-4 py-2 text-sm font-medium text-white disabled:opacity-50 ${
                  confirmationAction.confirmVariant === 'danger'
                    ? 'bg-red-600 hover:bg-red-700'
                    : 'bg-blue-600 hover:bg-blue-700'
                }`}
                onClick={async () => {
                  const action = confirmationAction;
                  setConfirmationAction(null);
                  await action.run();
                }}
                disabled={rejectMutation.isPending || publishMutation.isPending || rollbackMutation.isPending}
              >
                {confirmationAction.confirmLabel}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
