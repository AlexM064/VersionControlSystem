import { ReactNode } from 'react';

interface SkeletonProps {
  className?: string;
}

export const Skeleton = ({ className = '' }: SkeletonProps) => (
  <div className={`animate-pulse rounded-md bg-slate-200 dark:bg-slate-700 ${className}`} />
);

interface TableSkeletonProps {
  rows?: number;
  columns?: number;
}

export const TableSkeleton = ({ rows = 6, columns = 6 }: TableSkeletonProps) => {
  return (
    <div className="overflow-x-auto">
      <table className="w-full">
        <thead className="border-b border-slate-200 bg-slate-50 dark:border-slate-700 dark:bg-slate-800/50">
          <tr>
            {Array.from({ length: columns }).map((_, index) => (
              <th key={`head-${index}`} className="px-6 py-3 text-left">
                <Skeleton className="h-3 w-20" />
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-200 dark:divide-slate-700">
          {Array.from({ length: rows }).map((_, rowIndex) => (
            <tr key={`row-${rowIndex}`}>
              {Array.from({ length: columns }).map((_, colIndex) => (
                <td key={`cell-${rowIndex}-${colIndex}`} className="px-6 py-4">
                  <Skeleton className="h-4 w-full max-w-[180px]" />
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

interface EmptyStateIconProps {
  icon: ReactNode;
}

export const EmptyStateIcon = ({ icon }: EmptyStateIconProps) => (
  <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-slate-100 text-slate-500 dark:bg-slate-800 dark:text-slate-300">
    {icon}
  </div>
);