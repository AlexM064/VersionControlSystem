import { ReactNode } from 'react';

export interface CardProps {
  children: ReactNode;
  className?: string;
  hover?: boolean;
}

export const Card = ({ children, className = '', hover = false }: CardProps) => {
  return (
    <div
      className={`rounded-xl border border-slate-200/90 bg-white shadow-sm transition-all duration-200 dark:border-slate-700/90 dark:bg-slate-900 dark:shadow-black/20 ${hover ? 'hover:-translate-y-0.5 hover:border-slate-300 hover:shadow-md dark:hover:border-slate-600' : ''} ${className}`}
    >
      {children}
    </div>
  );
};

export interface CardHeaderProps {
  title?: string;
  subtitle?: string;
  action?: ReactNode;
  children?: ReactNode;
}

export const CardHeader = ({ title, subtitle, action, children }: CardHeaderProps) => {
  return (
    <div className="border-b border-slate-200 dark:border-slate-700 px-6 py-5">
      <div className="flex items-start justify-between gap-4">
        <div>
          {title && <h3 className="text-xl font-semibold tracking-tight text-slate-900 dark:text-slate-50">{title}</h3>}
          {subtitle && <p className="mt-1.5 text-sm leading-6 text-slate-600 dark:text-slate-400">{subtitle}</p>}
        </div>
        {action && <div className="flex-shrink-0">{action}</div>}
      </div>
      {children}
    </div>
  );
};

export interface CardBodyProps {
  children: ReactNode;
  className?: string;
}

export const CardBody = ({ children, className = '' }: CardBodyProps) => {
  return <div className={`px-6 py-5 ${className}`}>{children}</div>;
};

export interface CardFooterProps {
  children: ReactNode;
  className?: string;
}

export const CardFooter = ({ children, className = '' }: CardFooterProps) => {
  return <div className={`px-6 py-5 border-t border-slate-200 dark:border-slate-700 flex gap-2 justify-end ${className}`}>{children}</div>;
};
