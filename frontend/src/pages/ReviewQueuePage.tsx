import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Alert, Button, EmptyStateIcon, TableSkeleton } from '@/components/ui';
import { usePermission, useReviewQueue } from '@/hooks';
import { formatDate } from '@/utils/helpers';
import { ClipboardCheck } from 'lucide-react';
import { useToast } from '@/contexts/ToastContext';

export const ReviewQueuePage = () => {
  const navigate = useNavigate();
  const { can } = usePermission();
  const toast = useToast();
  const { data: queueItems = [], isLoading, isFetching, error, refetch } = useReviewQueue();

  useEffect(() => {
    if (!can('APPROVE_VERSION') && !can('REJECT_VERSION')) {
      navigate('/documents');
    }
  }, [can, navigate]);

  useEffect(() => {
    if (!error) return;

    const message = error instanceof Error ? error.message : 'An unexpected error occurred';
    toast.error(message, 'Failed to load review queue');
  }, [error, toast]);

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-slate-900 dark:text-slate-50 md:text-4xl">Review Queue</h1>
          <p className="mt-1.5 text-sm leading-7 text-slate-600 dark:text-slate-300">Versions currently awaiting review approval</p>
        </div>
        <Button type="button" variant="secondary" size="sm" onClick={() => refetch()} loading={isFetching && !isLoading}>
          Refresh
        </Button>
      </div>

      {error && (
        <Alert
          type="error"
          title="Failed to load review queue"
          message={error instanceof Error ? error.message : 'An unexpected error occurred'}
          dismissible={false}
        />
      )}

      {isLoading && (
        <div className="bg-white dark:bg-slate-900 rounded-lg border border-slate-200 dark:border-slate-700 overflow-hidden shadow-sm">
          <TableSkeleton rows={7} columns={7} />
        </div>
      )}

      {!isLoading && !error && queueItems.length === 0 && (
        <div className="bg-white dark:bg-slate-900 rounded-lg border border-slate-200 dark:border-slate-700 p-12 text-center">
          <EmptyStateIcon icon={<ClipboardCheck size={20} />} />
          <h3 className="text-lg font-medium text-slate-900 dark:text-slate-50 mb-1">No versions in review</h3>
          <p className="text-slate-600 dark:text-slate-400 text-sm">All submitted versions have been processed.</p>
          <div className="mt-4">
            <Button type="button" variant="secondary" size="sm" onClick={() => navigate('/documents')}>
              Browse Documents
            </Button>
          </div>
        </div>
      )}

      {!isLoading && !error && queueItems.length > 0 && (
        <div className="bg-white dark:bg-slate-900 rounded-lg border border-slate-200 dark:border-slate-700 overflow-hidden shadow-sm">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-slate-50 dark:bg-slate-800 border-b border-slate-200 dark:border-slate-700">
                <tr>
                  <th className="px-6 py-3.5 text-left text-xs font-semibold text-slate-800 dark:text-slate-100 uppercase tracking-wider">
                    Document
                  </th>
                  <th className="px-6 py-3.5 text-left text-xs font-semibold text-slate-800 dark:text-slate-100 uppercase tracking-wider">
                    Version
                  </th>
                  <th className="px-6 py-3.5 text-left text-xs font-semibold text-slate-800 dark:text-slate-100 uppercase tracking-wider">
                    Message
                  </th>
                  <th className="px-6 py-3.5 text-left text-xs font-semibold text-slate-800 dark:text-slate-100 uppercase tracking-wider">
                    Owner
                  </th>
                  <th className="px-6 py-3.5 text-left text-xs font-semibold text-slate-800 dark:text-slate-100 uppercase tracking-wider">
                    Submitted By
                  </th>
                  <th className="px-6 py-3.5 text-left text-xs font-semibold text-slate-800 dark:text-slate-100 uppercase tracking-wider">
                    Submitted At
                  </th>
                  <th className="px-6 py-3.5 text-left text-xs font-semibold text-slate-800 dark:text-slate-100 uppercase tracking-wider">
                    Action
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200 dark:divide-slate-700">
                {queueItems.map((item) => (
                  <tr key={`${item.documentId}-${item.versionId}`} className="transition-colors duration-150 hover:bg-slate-50/70 dark:hover:bg-slate-800/60">
                    <td className="px-6 py-4.5 text-sm font-medium text-slate-900 dark:text-slate-50">{item.documentTitle}</td>
                    <td className="px-6 py-4.5 text-sm text-slate-600 dark:text-slate-400">v{item.versionNumber}</td>
                    <td className="px-6 py-4.5 text-sm text-slate-700 dark:text-slate-300 max-w-sm truncate">{item.message || '—'}</td>
                    <td className="px-6 py-4.5 text-sm text-slate-600 dark:text-slate-400">{item.documentOwnerUsername}</td>
                    <td className="px-6 py-4.5 text-sm text-slate-600 dark:text-slate-400">{item.createdByUsername}</td>
                    <td className="px-6 py-4.5 text-sm text-slate-600 dark:text-slate-400">{formatDate(item.createdAt)}</td>
                    <td className="px-6 py-4.5 text-sm">
                      <Button
                        type="button"
                        variant="primary"
                        size="sm"
                        onClick={() => navigate(`/documents/${item.documentId}?versionId=${item.versionId}`)}
                      >
                        Open Document
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};
