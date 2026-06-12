import { Document } from '@/types/document';
import { StatusBadge, SectionCard } from '@/components/ui';
import { formatDate } from '@/utils/helpers';
import { Calendar, User } from 'lucide-react';

interface DocumentMetadataCardProps {
  document: Document;
}

export const DocumentMetadataCard = ({ document }: DocumentMetadataCardProps) => {
  return (
    <SectionCard>
      <div className="p-6">
      {/* Header */}
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0 flex-1">
          <h1 className="text-3xl font-bold tracking-tight text-slate-900 dark:text-slate-50">{document.title}</h1>
          <p className="mt-2 line-clamp-2 text-sm leading-6 text-slate-600 dark:text-slate-400">{document.description}</p>
        </div>
        <StatusBadge status={document.status} className="flex-shrink-0" />
      </div>

      {/* Metadata Grid */}
      <div className="mt-6 grid grid-cols-1 gap-6 border-t border-slate-200 dark:border-slate-700 pt-6 md:grid-cols-2">
        {/* Owner */}
        <div className="flex items-start gap-3">
          <User size={18} className="text-slate-400 dark:text-slate-500 mt-0.5 flex-shrink-0" />
          <div>
            <p className="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">Owner</p>
            <p className="text-sm text-slate-900 dark:text-slate-50 font-medium">{document.ownerUsername}</p>
          </div>
        </div>

        {/* Published Version */}
        <div className="flex items-start gap-3">
          <div className="text-slate-400 dark:text-slate-500 mt-0.5 flex-shrink-0">📦</div>
          <div>
            <p className="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">
              Published Version
            </p>
            <p className="text-sm text-slate-900 dark:text-slate-50 font-medium">
              {document.publishedVersionId ? `v${document.publishedVersionId}` : 'Not published'}
            </p>
          </div>
        </div>

        {/* Created At */}
        <div className="flex items-start gap-3">
          <Calendar size={18} className="text-slate-400 dark:text-slate-500 mt-0.5 flex-shrink-0" />
          <div>
            <p className="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">Created</p>
            <p className="text-sm text-slate-900 dark:text-slate-50">{formatDate(document.createdAt)}</p>
          </div>
        </div>

        {/* Updated At */}
        <div className="flex items-start gap-3">
          <Calendar size={18} className="text-slate-400 dark:text-slate-500 mt-0.5 flex-shrink-0" />
          <div>
            <p className="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">Updated</p>
            <p className="text-sm text-slate-900 dark:text-slate-50">{formatDate(document.updatedAt)}</p>
          </div>
        </div>
      </div>
      </div>
    </SectionCard>
  );
};
