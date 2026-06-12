import { Card } from './Card';

export interface StatsCardProps {
  title: string;
  value: number | string;
  subtitle?: string;
  icon?: React.ReactNode;
  loading?: boolean;
  className?: string;
}

export const StatsCard = ({ title, value, subtitle, icon, loading = false, className = '' }: StatsCardProps) => {
  return (
    <Card className={`p-6 transition-transform duration-200 hover:-translate-y-0.5 hover:shadow-md ${className}`}>
      <div className="flex items-center justify-between gap-4">
        <div className="min-w-0 flex-1">
          <p className="text-xs font-semibold uppercase tracking-wider text-slate-600 dark:text-slate-400">{title}</p>
          {loading ? (
            <div className="mt-2 h-8 bg-slate-200 dark:bg-slate-700 rounded animate-pulse"></div>
          ) : (
            <p className="text-3xl font-bold text-slate-900 dark:text-slate-50 mt-2">{value}</p>
          )}
          {subtitle && !loading && <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">{subtitle}</p>}
        </div>
        {icon && (
          <div className="text-slate-500 dark:text-slate-300 flex-shrink-0 h-11 w-11 rounded-lg bg-slate-100 dark:bg-slate-800 flex items-center justify-center">
            {icon}
          </div>
        )}
      </div>
    </Card>
  );
};