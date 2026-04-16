import { useState, useCallback, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Edit, Eye, Archive, FileSearch, Trash2 } from 'lucide-react';
import { useDocuments } from '@/hooks/useDocuments';
import { useArchiveDocument, useDeleteDocument } from '@/hooks/useDocumentMutations';
import { usePermission } from '@/hooks/usePermission';
import { useAuth } from '@/contexts/AuthContext';
import { DocumentFilterBar } from '@/components/filters/DocumentFilterBar';
import { StatusBadge, Button, Alert, PageShell, PageHeader, SectionCard, StatePanel, TableSkeleton, EmptyStateIcon } from '@/components/ui';
import { Document, DocumentStatus } from '@/types/document';
import { UserRole } from '@/types/auth';
import { formatDate } from '@/utils/helpers';
import { useToast } from '@/contexts/ToastContext';
import { getApiErrorMessage } from '@/api';

const PAGE_SIZE = 10;

export const DocumentsPage = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { can } = usePermission();
  const toast = useToast();
  const isAdmin = user?.roles.includes(UserRole.ADMIN);
  const isAuthor = user?.roles.includes(UserRole.AUTHOR);
  const isReviewer = user?.roles.includes(UserRole.REVIEWER);
  const isReaderOnly = !isAdmin && !isAuthor && !isReviewer && user?.roles.includes(UserRole.READER);

  const [page, setPage] = useState(1);
  const [titleFilter, setTitleFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState<DocumentStatus | ''>('');
  const [archiveConfirmId, setArchiveConfirmId] = useState<number | null>(null);
  const [deleteConfirmId, setDeleteConfirmId] = useState<number | null>(null);

  const {
    data: documentsData,
    isLoading,
    isError,
    error,
    refetch,
  } = useDocuments({
    page,
    pageSize: PAGE_SIZE,
    title: titleFilter,
    status: statusFilter,
    publishedOnly: isReaderOnly,
  });

  const archiveMutation = useArchiveDocument();
  const deleteMutation = useDeleteDocument();

  useEffect(() => {
    if (!isError) return;

    const message = error instanceof Error ? error.message : 'An error occurred while loading documents';
    toast.error(message, 'Failed to load documents');
  }, [isError, error, toast]);

  const handleTitleFilterChange = useCallback((title: string) => {
    setTitleFilter(title);
    setPage(1); // Reset to first page
  }, []);

  const handleStatusFilterChange = useCallback((status: DocumentStatus | '') => {
    setStatusFilter(status);
    setPage(1); // Reset to first page
  }, []);

  const handleResetFilters = useCallback(() => {
    setTitleFilter('');
    setStatusFilter('');
    setPage(1);
  }, []);

  const handleViewDetails = (documentId: number) => {
    navigate(`/documents/${documentId}`);
  };

  const handleEditDocument = (documentId: number) => {
    navigate(`/documents/${documentId}/edit`);
  };

  const handleArchiveDocument = (documentId: number) => {
    setArchiveConfirmId(documentId);
  };

  const handleDeleteDocument = (documentId: number) => {
    setDeleteConfirmId(documentId);
  };

  const confirmArchiveDocument = async () => {
    if (!archiveConfirmId) return;

    try {
      await archiveMutation.mutateAsync(archiveConfirmId);
      toast.success('Document archived successfully.');
      setArchiveConfirmId(null);
      refetch(); // Refresh the list
    } catch (archiveError: unknown) {
      const message = getApiErrorMessage(archiveError, 'Failed to archive document');
      toast.error(message, 'Archive failed');
    }
  };

  const cancelArchiveDocument = () => {
    setArchiveConfirmId(null);
  };

  const confirmDeleteDocument = async () => {
    if (!deleteConfirmId) return;

    try {
      await deleteMutation.mutateAsync(deleteConfirmId);
      toast.success('Document permanently deleted.');
      setDeleteConfirmId(null);
      refetch();
    } catch (deleteError: unknown) {
      const message = getApiErrorMessage(deleteError, 'Failed to permanently delete document');
      toast.error(message, 'Delete failed');
    }
  };

  const cancelDeleteDocument = () => {
    setDeleteConfirmId(null);
  };

  const totalPages = documentsData?.totalPages || 1;
  const documents = documentsData?.content || [];

  return (
    <PageShell>
      <div className="space-y-8">
        <PageHeader
          title="Documents"
          description={isReaderOnly ? 'Browse published documents in read-only mode.' : 'Manage document versions and workflows.'}
          actions={can('CREATE_DOCUMENT') ? (
            <Button variant="primary" size="md" onClick={() => navigate('/documents/create')} className="flex items-center gap-2">
              <Plus size={18} />
              Create Document
            </Button>
          ) : undefined}
        />

        {/* Filters */}
        <SectionCard>
          <div className="p-4 md:p-6">
            <DocumentFilterBar
              titleFilter={titleFilter}
              statusFilter={statusFilter}
              onTitleChange={handleTitleFilterChange}
              onStatusChange={handleStatusFilterChange}
              onReset={handleResetFilters}
              showStatusFilter={!isReaderOnly}
            />
          </div>
        </SectionCard>

        {/* Error State */}
        {isError && (
          <Alert
            type="error"
            title="Failed to load documents"
            message={error instanceof Error ? error.message : 'An error occurred while loading documents'}
            dismissible={false}
          />
        )}

        {/* Loading State */}
        {isLoading && (
          <SectionCard>
            <TableSkeleton rows={8} columns={9} />
          </SectionCard>
        )}

        {/* Empty State */}
        {!isLoading && documents.length === 0 && (
          <StatePanel
            title="No documents found"
            message={titleFilter || statusFilter ? 'Try adjusting your filters.' : 'Create your first document to get started.'}
            icon={<EmptyStateIcon icon={<FileSearch size={20} />} />}
            actions={can('CREATE_DOCUMENT') && !(titleFilter || statusFilter) ? (
              <Button variant="primary" size="sm" onClick={() => navigate('/documents/create')}>
                Create Document
              </Button>
            ) : undefined}
          />
        )}

        {/* Documents Table */}
        {!isLoading && documents.length > 0 && (
          <SectionCard>
            <div className="overflow-x-auto">
              <table className="w-full">
              <thead className="bg-slate-50 border-b border-slate-200 dark:bg-slate-800 dark:border-slate-700">
                <tr>
                  <th className="px-6 py-3.5 text-left text-xs font-semibold text-slate-800 dark:text-slate-100 uppercase tracking-wider">
                    ID
                  </th>
                  <th className="px-6 py-3.5 text-left text-xs font-semibold text-slate-800 dark:text-slate-100 uppercase tracking-wider">
                    Title
                  </th>
                  <th className="px-6 py-3.5 text-left text-xs font-semibold text-slate-800 dark:text-slate-100 uppercase tracking-wider">
                    Description
                  </th>
                  <th className="px-6 py-3.5 text-left text-xs font-semibold text-slate-800 dark:text-slate-100 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-3.5 text-left text-xs font-semibold text-slate-800 dark:text-slate-100 uppercase tracking-wider">
                    Owner
                  </th>
                  <th className="px-6 py-3.5 text-left text-xs font-semibold text-slate-800 dark:text-slate-100 uppercase tracking-wider">
                    Published Version
                  </th>
                  <th className="px-6 py-3.5 text-left text-xs font-semibold text-slate-800 dark:text-slate-100 uppercase tracking-wider">
                    Created
                  </th>
                  <th className="px-6 py-3.5 text-left text-xs font-semibold text-slate-800 dark:text-slate-100 uppercase tracking-wider">
                    Updated
                  </th>
                  <th className="px-6 py-3.5 text-left text-xs font-semibold text-slate-800 dark:text-slate-100 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200 dark:divide-slate-700">
                {documents.map((doc: Document) => (
                  <tr key={doc.id} className="transition-colors duration-150 hover:bg-slate-50/70 dark:hover:bg-slate-800/60">
                    <td className="px-6 py-4.5 text-sm text-slate-600 dark:text-slate-400">#{doc.id}</td>
                    <td className="px-6 py-4.5 text-sm">
                      <div className="font-medium text-slate-900 dark:text-slate-50">{doc.title}</div>
                    </td>
                    <td className="px-6 py-4.5 text-sm text-slate-600 dark:text-slate-400 max-w-xs">
                      <div className="truncate" title={doc.description}>
                        {doc.description || '—'}
                      </div>
                    </td>
                    <td className="px-6 py-4.5 text-sm">
                      <StatusBadge status={doc.status} />
                    </td>
                    <td className="px-6 py-4.5 text-sm text-slate-600 dark:text-slate-400">{doc.ownerUsername}</td>
                    <td className="px-6 py-4.5 text-sm text-slate-600 dark:text-slate-400">
                      {doc.publishedVersionId ? `v${doc.publishedVersionId}` : '—'}
                    </td>
                    <td className="px-6 py-4.5 text-sm text-slate-600 dark:text-slate-400">
                      {formatDate(doc.createdAt)}
                    </td>
                    <td className="px-6 py-4.5 text-sm text-slate-600 dark:text-slate-400">
                      {formatDate(doc.updatedAt)}
                    </td>
                    <td className="px-6 py-4.5 text-sm">
                      <div className="flex gap-2">
                        <button
                          onClick={() => handleViewDetails(doc.id)}
                          className="p-1.5 text-slate-600 hover:bg-blue-100 hover:text-blue-700 dark:text-slate-400 dark:hover:bg-blue-900/40 dark:hover:text-blue-300 rounded transition-colors duration-150"
                          title="View details"
                          disabled={archiveMutation.isPending || deleteMutation.isPending}
                        >
                          <Eye size={16} />
                        </button>

                        {(can('EDIT_DOCUMENT') || (user?.username === doc.ownerUsername && can('CREATE_VERSION'))) && (
                          <button
                            onClick={() => handleEditDocument(doc.id)}
                            className="p-1.5 text-slate-600 hover:bg-blue-100 hover:text-blue-700 dark:text-slate-400 dark:hover:bg-blue-900/40 dark:hover:text-blue-300 rounded transition-colors duration-150"
                            title="Edit document"
                            disabled={archiveMutation.isPending || deleteMutation.isPending}
                          >
                            <Edit size={16} />
                          </button>
                        )}

                        {can('ARCHIVE_DOCUMENT') && doc.status === DocumentStatus.ACTIVE && (
                          <button
                            onClick={() => handleArchiveDocument(doc.id)}
                            className="p-1.5 text-slate-600 hover:bg-red-100 hover:text-red-700 dark:text-slate-400 dark:hover:bg-red-900/40 dark:hover:text-red-300 rounded transition-colors duration-150"
                            title="Archive document"
                            disabled={archiveMutation.isPending || deleteMutation.isPending}
                          >
                            <Archive size={16} />
                          </button>
                        )}

                        {isAdmin && (
                          <button
                            onClick={() => handleDeleteDocument(doc.id)}
                            className="p-1.5 text-slate-600 hover:bg-red-100 hover:text-red-700 dark:text-slate-400 dark:hover:bg-red-900/40 dark:hover:text-red-300 rounded transition-colors duration-150"
                            title="Permanently delete document"
                            disabled={archiveMutation.isPending || deleteMutation.isPending}
                          >
                            <Trash2 size={16} />
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
              </table>
            </div>

            {/* Archive Confirmation Modal */}
            {archiveConfirmId && (
              <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
                <div className="mx-4 w-full max-w-md rounded-lg bg-white dark:bg-slate-900 p-6 shadow-xl dark:shadow-2xl dark:shadow-black/30">
                  <h3 className="text-lg font-semibold tracking-tight text-slate-900 dark:text-slate-50">Archive Document</h3>
                  <p className="mt-3 text-sm leading-6 text-slate-600 dark:text-slate-400">
                    Are you sure you want to archive this document? This action cannot be undone.
                  </p>
                  <div className="mt-6 flex justify-end gap-3">
                    <Button variant="secondary" size="sm" onClick={cancelArchiveDocument} disabled={archiveMutation.isPending}>
                      Cancel
                    </Button>
                    <Button variant="danger" size="sm" onClick={confirmArchiveDocument} loading={archiveMutation.isPending}>
                      Archive
                    </Button>
                  </div>
                </div>
              </div>
            )}

            {/* Delete Confirmation Modal */}
            {deleteConfirmId && (
              <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
                <div className="mx-4 w-full max-w-md rounded-lg bg-white dark:bg-slate-900 p-6 shadow-xl dark:shadow-2xl dark:shadow-black/30">
                  <h3 className="text-lg font-semibold tracking-tight text-slate-900 dark:text-slate-50">Delete Document Permanently</h3>
                  <p className="mt-3 text-sm leading-6 text-slate-600 dark:text-slate-400">
                    Are you sure you want to permanently delete this document? This action cannot be undone.
                  </p>
                  <div className="mt-6 flex justify-end gap-3">
                    <Button variant="secondary" size="sm" onClick={cancelDeleteDocument} disabled={deleteMutation.isPending}>
                      Cancel
                    </Button>
                    <Button variant="danger" size="sm" onClick={confirmDeleteDocument} loading={deleteMutation.isPending}>
                      Delete Permanently
                    </Button>
                  </div>
                </div>
              </div>
            )}

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="flex items-center justify-between border-t border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800/50 px-6 py-4">
                <div className="text-sm text-slate-600 dark:text-slate-400">
                  Page <span className="font-medium">{page}</span> of <span className="font-medium">{totalPages}</span> ({documentsData?.totalElements || 0} total)
                </div>
                <div className="flex gap-2">
                  <Button variant="secondary" size="sm" disabled={page === 1} onClick={() => setPage(page - 1)} className="flex items-center gap-1">
                    ← Previous
                  </Button>
                  <Button variant="secondary" size="sm" disabled={page === totalPages} onClick={() => setPage(page + 1)} className="flex items-center gap-1">
                    Next →
                  </Button>
                </div>
              </div>
            )}
          </SectionCard>
        )}
      </div>
    </PageShell>
  );
};
