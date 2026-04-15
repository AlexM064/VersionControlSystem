import { DocumentVersion } from '@/types/version';
import { StatusBadge, Button, SectionCard, StatePanel, TableSkeleton, EmptyStateIcon } from '@/components/ui';
import { formatDate } from '@/utils/helpers';
import { Eye, Copy, FileDown, Files } from 'lucide-react';

interface VersionsTableProps {
  versions: DocumentVersion[];
  isLoading?: boolean;
  currentPage?: number;
  totalPages?: number;
  onPageChange?: (page: number) => void;
  onViewVersion?: (version: DocumentVersion) => void;
  onCompare?: (version: DocumentVersion) => void;
  onDownloadPdf?: (version: DocumentVersion) => void;
  downloadingVersionId?: number | null;
}

export const VersionsTable = ({
  versions,
  isLoading = false,
  currentPage = 1,
  totalPages = 1,
  onPageChange,
  onViewVersion,
  onCompare,
  onDownloadPdf,
  downloadingVersionId = null,
}: VersionsTableProps) => {
  if (isLoading) {
    return (
      <SectionCard>
        <TableSkeleton rows={5} columns={7} />
      </SectionCard>
    );
  }

  if (versions.length === 0) {
    return (
      <SectionCard>
        <div className="p-6">
          <StatePanel title="No versions yet" message="Create a new version to get started." icon={<EmptyStateIcon icon={<Files size={20} />} />} />
        </div>
      </SectionCard>
    );
  }

  return (
    <div className="space-y-4">
      <div className="overflow-x-auto rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 shadow-sm">
        <table className="w-full">
          <thead className="bg-slate-50 dark:bg-slate-800 border-b border-slate-200 dark:border-slate-700">
            <tr>
              <th className="px-6 py-3.5 text-left text-xs font-semibold text-slate-800 dark:text-slate-100 uppercase tracking-wider">
                ID
              </th>
              <th className="px-6 py-3.5 text-left text-xs font-semibold text-slate-800 dark:text-slate-100 uppercase tracking-wider">
                Version
              </th>
              <th className="px-6 py-3.5 text-left text-xs font-semibold text-slate-800 dark:text-slate-100 uppercase tracking-wider">
                Message
              </th>
              <th className="px-6 py-3.5 text-left text-xs font-semibold text-slate-800 dark:text-slate-100 uppercase tracking-wider">
                Status
              </th>
              <th className="px-6 py-3.5 text-left text-xs font-semibold text-slate-800 dark:text-slate-100 uppercase tracking-wider">
                Created By
              </th>
              <th className="px-6 py-3.5 text-left text-xs font-semibold text-slate-800 dark:text-slate-100 uppercase tracking-wider">
                Created At
              </th>
              <th className="px-6 py-3.5 text-left text-xs font-semibold text-slate-800 dark:text-slate-100 uppercase tracking-wider">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200 dark:divide-slate-700">
            {versions.map((version) => (
              <tr key={version.id} className="transition-colors duration-150 hover:bg-slate-50/70 dark:hover:bg-slate-800/60">
                <td className="px-6 py-4.5 text-sm text-slate-600 dark:text-slate-400">#{version.id}</td>
                <td className="px-6 py-4.5 text-sm">
                  <span className="font-mono font-bold text-blue-600">v{version.versionNumber}</span>
                </td>
                <td className="px-6 py-4.5 text-sm text-slate-900 dark:text-slate-50 max-w-xs truncate">
                  {version.message || '—'}
                </td>
                <td className="px-6 py-4.5 text-sm">
                  <StatusBadge status={version.status} />
                </td>
                <td className="px-6 py-4.5 text-sm text-slate-600 dark:text-slate-400">{version.createdByUsername}</td>
                <td className="px-6 py-4.5 text-sm text-slate-600 dark:text-slate-400">{formatDate(version.createdAt)}</td>
                <td className="px-6 py-4.5 text-sm">
                  <div className="flex gap-1">
                    {onViewVersion && (
                      <button
                        onClick={() => onViewVersion(version)}
                        className="p-1.5 text-slate-600 dark:text-slate-400 hover:bg-blue-50 dark:hover:bg-blue-950/40 hover:text-blue-600 dark:hover:text-blue-300 rounded transition-colors duration-150"
                        title="View version"
                      >
                        <Eye size={16} />
                      </button>
                    )}
                    {onCompare && (
                      <button
                        onClick={() => onCompare(version)}
                        className="p-1.5 text-slate-600 dark:text-slate-400 hover:bg-green-50 dark:hover:bg-green-950/40 hover:text-green-600 dark:hover:text-green-300 rounded transition-colors duration-150"
                        title="Compare version"
                      >
                        <Copy size={16} />
                      </button>
                    )}
                    {onDownloadPdf && (
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        loading={downloadingVersionId === version.id}
                        disabled={downloadingVersionId === version.id}
                        onClick={() => onDownloadPdf(version)}
                        className="p-1.5 h-8 w-8 flex items-center justify-center"
                        title="Download PDF"
                      >
                        <FileDown size={16} />
                      </Button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-between">
          <div className="text-sm text-slate-600 dark:text-slate-400">
            Page <span className="font-medium">{currentPage}</span> of{' '}
            <span className="font-medium">{totalPages}</span>
          </div>
          <div className="flex gap-2">
            <Button
              variant="secondary"
              size="sm"
              disabled={currentPage === 1}
              onClick={() => onPageChange?.(currentPage - 1)}
              className="flex items-center gap-1"
            >
              ← Previous
            </Button>
            <Button
              variant="secondary"
              size="sm"
              disabled={currentPage === totalPages}
              onClick={() => onPageChange?.(currentPage + 1)}
              className="flex items-center gap-1"
            >
              Next →
            </Button>
          </div>
        </div>
      )}
    </div>
  );
};
