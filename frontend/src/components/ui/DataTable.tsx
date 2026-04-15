import { Search, ChevronLeft, ChevronRight } from 'lucide-react';
import { Button } from './common';

export interface DataTableProps<T> {
  columns: Array<{
    key: string;
    label: string;
    render?: (value: any, row: T) => React.ReactNode;
  }>;
  data: T[];
  isLoading?: boolean;
  currentPage?: number;
  totalPages?: number;
  onPageChange?: (page: number) => void;
  searchable?: boolean;
  onSearch?: (query: string) => void;
  rowKey?: (row: T) => string;
  onRowClick?: (row: T) => void;
}

export const DataTable = <T extends Record<string, any>>({
  columns,
  data,
  isLoading,
  currentPage = 1,
  totalPages = 1,
  onPageChange,
  searchable = false,
  onSearch,
  rowKey = (row) => row.id,
  onRowClick,
}: DataTableProps<T>) => {
  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {searchable && (
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-slate-400 dark:text-slate-500" size={20} />
          <input
            type="text"
            placeholder="Search..."
            onChange={(e) => onSearch?.(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border border-slate-200 dark:border-slate-700 rounded-lg bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-50 placeholder-slate-400 dark:placeholder-slate-500 focus:ring-2 focus:ring-blue-600 focus:border-transparent transition-all duration-150"
          />
        </div>
      )}

      <div className="overflow-x-auto border border-slate-200 dark:border-slate-700 rounded-lg">
        <table className="w-full">
          <thead className="bg-slate-50 dark:bg-slate-800 border-b border-slate-200 dark:border-slate-700">
            <tr>
              {columns.map((column) => (
                <th key={column.key} className="px-6 py-3 text-left text-xs font-semibold text-slate-900 dark:text-slate-100 uppercase tracking-wider">
                  {column.label}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200 dark:divide-slate-700">
            {data.length === 0 ? (
              <tr>
                <td colSpan={columns.length} className="px-6 py-8 text-center text-slate-500 dark:text-slate-400">
                  No data available
                </td>
              </tr>
            ) : (
              data.map((row) => (
                <tr
                  key={rowKey(row)}
                  onClick={() => onRowClick?.(row)}
                  className={`transition-colors duration-150 ${onRowClick ? 'hover:bg-slate-50 dark:hover:bg-slate-800 cursor-pointer' : ''}`}
                >
                  {columns.map((column) => (
                    <td key={column.key} className="px-6 py-4 text-sm text-slate-900 dark:text-slate-100">
                      {column.render ? column.render(row[column.key], row) : row[column.key]}
                    </td>
                  ))}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {totalPages > 1 && (
        <div className="flex justify-between items-center mt-4 px-1">
          <p className="text-sm text-slate-600 dark:text-slate-400">
            Page <span className="font-medium">{currentPage}</span> of <span className="font-medium">{totalPages}</span>
          </p>
          <div className="flex gap-2">
            <Button
              variant="secondary"
              size="sm"
              disabled={currentPage === 1}
              onClick={() => onPageChange?.(currentPage - 1)}
            >
              <ChevronLeft size={16} />
            </Button>
            <Button
              variant="secondary"
              size="sm"
              disabled={currentPage === totalPages}
              onClick={() => onPageChange?.(currentPage + 1)}
            >
              <ChevronRight size={16} />
            </Button>
          </div>
        </div>
      )}
    </div>
  );
};
