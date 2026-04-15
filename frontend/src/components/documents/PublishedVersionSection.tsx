import { DocumentVersion } from '@/types/version';
import { StatusBadge, Button, SectionCard, StatePanel } from '@/components/ui';
import { formatDate } from '@/utils/helpers';
import { FileText, FileDown } from 'lucide-react';

interface PublishedVersionSectionProps {
  publishedVersion: DocumentVersion | null | undefined;
  isLoading?: boolean;
  onDownloadPdf?: () => void;
  isDownloadingPdf?: boolean;
}

export const PublishedVersionSection = ({
  publishedVersion,
  isLoading,
  onDownloadPdf,
  isDownloadingPdf = false,
}: PublishedVersionSectionProps) => {
  if (isLoading) {
    return (
      <SectionCard>
        <div className="p-6">
          <div className="mb-4 flex items-center gap-2">
            <FileText size={20} className="text-slate-400 dark:text-slate-500" />
            <h2 className="text-xl font-semibold tracking-tight text-slate-900 dark:text-slate-50">Published Version</h2>
          </div>
          <div className="h-20 animate-pulse rounded bg-slate-100 dark:bg-slate-800" />
        </div>
      </SectionCard>
    );
  }

  if (!publishedVersion) {
    return (
      <SectionCard>
        <div className="p-6">
          <div className="mb-4 flex items-center gap-2">
            <FileText size={20} className="text-slate-400 dark:text-slate-500" />
            <h2 className="text-xl font-semibold tracking-tight text-slate-900 dark:text-slate-50">Published Version</h2>
          </div>
          <StatePanel title="No published version yet" message="The current document has not been published." icon={<span className="text-xl">📭</span>} />
        </div>
      </SectionCard>
    );
  }

  return (
    <SectionCard className="border-green-200 bg-gradient-to-br from-green-50 to-white dark:border-slate-700 dark:bg-slate-900 dark:from-slate-900 dark:to-slate-900 dark:shadow-sm dark:ring-1 dark:ring-slate-800/80">
      <div className="p-6">
        <div className="mb-5 flex items-center justify-between gap-4">
          <div className="flex items-center gap-2">
            <FileText size={20} className="text-green-600" />
            <h2 className="text-xl font-semibold tracking-tight text-slate-900 dark:text-slate-50">Published Version</h2>
          </div>
          <div className="flex items-center gap-2">
          <StatusBadge status={publishedVersion.status} className="dark:ring-1 dark:ring-slate-600/70" />
          {onDownloadPdf && (
            <Button
              type="button"
              variant="secondary"
              size="sm"
              onClick={onDownloadPdf}
              loading={isDownloadingPdf}
              disabled={isDownloadingPdf}
              className="flex items-center gap-2 dark:bg-slate-800 dark:border-slate-600"
            >
              <FileDown size={14} />
              PDF
            </Button>
          )}
        </div>
      </div>

      <div className="space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <div>
            <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">Version</p>
            <p className="mt-1 text-lg font-bold tracking-tight text-slate-900 dark:text-slate-50">v{publishedVersion.versionNumber}</p>
          </div>
          <div>
            <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">Published By</p>
            <p className="mt-1 text-sm text-slate-900 dark:text-slate-50">{publishedVersion.createdByUsername}</p>
          </div>
        </div>

        <div>
          <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">Published At</p>
          <p className="mt-1 text-sm text-slate-900 dark:text-slate-50">{formatDate(publishedVersion.createdAt)}</p>
        </div>

        {publishedVersion.message && (
          <div>
            <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">Message</p>
            <p className="mt-1 text-sm leading-6 text-slate-900 dark:text-slate-50 whitespace-pre-wrap">{publishedVersion.message}</p>
          </div>
        )}
      </div>
      </div>
    </SectionCard>
  );
};
