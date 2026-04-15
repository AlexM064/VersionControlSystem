import { Search, X } from 'lucide-react';
import { Button } from '@/components/ui';
import { DocumentStatus } from '@/types/document';

interface DocumentFilterBarProps {
  titleFilter: string;
  statusFilter: DocumentStatus | '';
  onTitleChange: (title: string) => void;
  onStatusChange: (status: DocumentStatus | '') => void;
  onReset: () => void;
  showStatusFilter?: boolean;
}

export const DocumentFilterBar = ({
  titleFilter,
  statusFilter,
  onTitleChange,
  onStatusChange,
  onReset,
  showStatusFilter = true,
}: DocumentFilterBarProps) => {
  const hasActiveFilters = titleFilter || (showStatusFilter && statusFilter);

  return (
    <div className="bg-white dark:bg-slate-900 rounded-lg border border-slate-200 dark:border-slate-700 p-4 space-y-3 shadow-sm">
      <div className="flex gap-3 flex-wrap items-end">
        {/* Title Search */}
        <div className="flex-1 min-w-xs">
          <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">Search by Title</label>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-slate-400 dark:text-slate-500" size={16} />
            <input
              type="text"
              placeholder="Document title..."
              value={titleFilter}
              onChange={(e) => onTitleChange(e.target.value)}
              className="w-full pl-9 pr-3 py-2.5 border border-slate-300 dark:border-slate-600 dark:bg-slate-800 dark:text-slate-50 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-150"
            />
          </div>
        </div>

        {/* Status Filter */}
        {showStatusFilter && (
          <div className="flex-1 min-w-xs">
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">Status</label>
            <select
              value={statusFilter}
              onChange={(e) => onStatusChange((e.target.value as DocumentStatus) || '')}
              className="w-full px-3 py-2.5 border border-slate-300 dark:border-slate-600 dark:bg-slate-800 dark:text-slate-50 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-150"
            >
              <option value="">All Statuses</option>
              <option value={DocumentStatus.ACTIVE}>Active</option>
              <option value={DocumentStatus.ARCHIVED}>Archived</option>
            </select>
          </div>
        )}

        {/* Reset Button */}
        {hasActiveFilters && (
          <Button
            variant="ghost"
            size="sm"
            onClick={onReset}
            className="h-[38px] flex items-center gap-1.5"
          >
            <X size={14} />
            Clear Filters
          </Button>
        )}
      </div>
    </div>
  );
};
