import { ReactNode } from 'react';
import { Button } from './common';
import { Card } from './Card';

export interface PageShellProps {
  children: ReactNode;
  className?: string;
}

export const PageShell = ({ children, className = '' }: PageShellProps) => {
  return <div className={`mx-auto w-full max-w-6xl px-4 py-8 md:px-6 lg:px-8 ${className}`}>{children}</div>;
};

export interface PageHeaderProps {
  title: string;
  description?: string;
  actions?: ReactNode;
}

export const PageHeader = ({ title, description, actions }: PageHeaderProps) => {
  return (
    <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between pb-1">
      <div className="min-w-0">
        <h1 className="text-3xl font-bold tracking-tight text-slate-900 dark:text-slate-50 md:text-4xl">{title}</h1>
        {description && <p className="mt-2.5 max-w-3xl text-sm leading-7 text-slate-600 dark:text-slate-300">{description}</p>}
      </div>
      {actions && <div className="flex flex-wrap gap-2">{actions}</div>}
    </div>
  );
};

export interface SectionCardProps {
  children: ReactNode;
  className?: string;
}

export const SectionCard = ({ children, className = '' }: SectionCardProps) => {
  return <Card className={`overflow-hidden border-slate-200 shadow-sm dark:border-slate-700 ${className}`}>{children}</Card>;
};

export interface StatePanelProps {
  icon?: ReactNode;
  title: string;
  message: string;
  actions?: ReactNode;
}

export const StatePanel = ({ icon, title, message, actions }: StatePanelProps) => {
  return (
    <div className="rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 px-6 py-11 text-center shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:shadow-md">
      {icon && <div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-lg bg-gradient-to-br from-slate-100 to-slate-50 dark:from-slate-800 dark:to-slate-900 text-slate-600 dark:text-slate-400 transition-transform duration-200">{icon}</div>}
      <h3 className="text-lg font-semibold tracking-tight text-slate-900 dark:text-slate-50">{title}</h3>
      <p className="mx-auto mt-2.5 max-w-xl text-sm leading-7 text-slate-600 dark:text-slate-300">{message}</p>
      {actions && <div className="mt-6 flex justify-center gap-3">{actions}</div>}
    </div>
  );
};

export interface InlineStateProps {
  type: 'loading' | 'error' | 'success' | 'empty';
  title: string;
  message: string;
  action?: ReactNode;
}

export const InlineState = ({ type, title, message, action }: InlineStateProps) => {
  const tone =
    type === 'error'
      ? 'border-red-200 dark:border-red-900/50 bg-red-50 dark:bg-red-950/30 text-red-700 dark:text-red-200'
      : type === 'success'
        ? 'border-green-200 dark:border-green-900/50 bg-green-50 dark:bg-green-950/30 text-green-700 dark:text-green-200'
        : type === 'empty'
          ? 'border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-900 text-slate-700 dark:text-slate-300'
          : 'border-blue-200 dark:border-blue-900/50 bg-blue-50 dark:bg-blue-950/30 text-blue-700 dark:text-blue-200';

  return (
    <div className={`rounded-lg border p-4 ${tone}`}>
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="font-semibold text-sm">{title}</p>
          <p className="text-sm opacity-90 leading-5">{message}</p>
        </div>
        {action}
      </div>
    </div>
  );
};

export interface ToolbarButtonProps {
  children: ReactNode;
  onClick?: () => void;
  disabled?: boolean;
}

export const ToolbarButton = ({ children, onClick, disabled }: ToolbarButtonProps) => {
  return (
    <Button variant="secondary" size="sm" onClick={onClick} disabled={disabled} className="min-w-24">
      {children}
    </Button>
  );
};