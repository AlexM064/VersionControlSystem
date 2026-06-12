import { AlertCircle, CheckCircle, Info, AlertTriangle } from 'lucide-react';
import { ReactNode } from 'react';

export interface AlertProps {
  type: 'success' | 'error' | 'warning' | 'info';
  title?: string;
  message: string;
  onClose?: () => void;
  dismissible?: boolean;
}

const alertStyles = {
  success: {
    bg: 'bg-green-50 dark:bg-green-950/30',
    border: 'border-green-200 dark:border-green-900/50',
    icon: <CheckCircle size={20} className="text-green-600 dark:text-green-500" />,
    title: 'text-green-900 dark:text-green-100',
    message: 'text-green-800 dark:text-green-200',
  },
  error: {
    bg: 'bg-red-50 dark:bg-red-950/30',
    border: 'border-red-200 dark:border-red-900/50',
    icon: <AlertCircle size={20} className="text-red-600 dark:text-red-500" />,
    title: 'text-red-900 dark:text-red-100',
    message: 'text-red-800 dark:text-red-200',
  },
  warning: {
    bg: 'bg-yellow-50 dark:bg-yellow-950/30',
    border: 'border-yellow-200 dark:border-yellow-900/50',
    icon: <AlertTriangle size={20} className="text-yellow-600 dark:text-yellow-500" />,
    title: 'text-yellow-900 dark:text-yellow-100',
    message: 'text-yellow-800 dark:text-yellow-200',
  },
  info: {
    bg: 'bg-blue-50 dark:bg-blue-950/30',
    border: 'border-blue-200 dark:border-blue-900/50',
    icon: <Info size={20} className="text-blue-600 dark:text-blue-500" />,
    title: 'text-blue-900 dark:text-blue-100',
    message: 'text-blue-800 dark:text-blue-200',
  },
};

export const Alert = ({ type, title, message, onClose, dismissible = true }: AlertProps) => {
  const styles = alertStyles[type];

  return (
    <div className={`${styles.bg} border ${styles.border} rounded-lg p-4 flex gap-3 items-start shadow-sm transition-colors duration-200`}>
      <div className="flex-shrink-0 mt-0.5">{styles.icon}</div>
      <div className="flex-1">
        {title && <h3 className={`font-semibold ${styles.title}`}>{title}</h3>}
        <p className={`text-sm leading-5 ${styles.message}`}>{message}</p>
      </div>
      {dismissible && onClose && (
        <button onClick={onClose} className="text-slate-400 hover:text-slate-600 dark:text-slate-500 dark:hover:text-slate-400 transition-colors duration-200 flex-shrink-0 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-400 rounded">
          ✕
        </button>
      )}
    </div>
  );
};

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
  loading?: boolean;
  children: ReactNode;
}

const buttonStyles = {
  primary: 'bg-blue-600 text-white shadow-sm hover:bg-blue-700 hover:shadow-md active:bg-blue-800 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 focus-visible:ring-offset-2 dark:focus-visible:ring-offset-slate-950 dark:hover:bg-blue-700 disabled:bg-slate-300 dark:disabled:bg-slate-700',
  secondary:
    'bg-slate-100 text-slate-900 border border-slate-200 shadow-sm hover:bg-slate-200 hover:shadow-md active:bg-slate-300 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 focus-visible:ring-offset-2 dark:focus-visible:ring-offset-slate-950 dark:bg-slate-800 dark:text-slate-100 dark:border-slate-700 dark:hover:bg-slate-700 dark:active:bg-slate-600 dark:disabled:bg-slate-800 dark:disabled:text-slate-500 disabled:bg-slate-50',
  danger: 'bg-red-600 text-white shadow-sm hover:bg-red-700 hover:shadow-md active:bg-red-800 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-500 focus-visible:ring-offset-2 dark:focus-visible:ring-offset-slate-950 disabled:bg-slate-300 dark:disabled:bg-slate-700',
  ghost: 'bg-transparent text-slate-700 hover:bg-slate-100 active:bg-slate-200 border border-slate-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 focus-visible:ring-offset-2 dark:focus-visible:ring-offset-slate-950 dark:text-slate-300 dark:hover:bg-slate-800 dark:active:bg-slate-700 dark:border-slate-600',
};

const sizeStyles = {
  sm: 'px-3 py-1.5 text-sm',
  md: 'px-4 py-2.5 text-sm',
  lg: 'px-6 py-3 text-base',
};

export const Button = ({
  variant = 'primary',
  size = 'md',
  loading = false,
  className = '',
  children,
  disabled,
  ...props
}: ButtonProps) => {
  return (
    <button
      className={`rounded-xl font-medium tracking-tight transition-all duration-150 active:scale-[0.99] disabled:opacity-50 disabled:cursor-not-allowed ${buttonStyles[variant]} ${sizeStyles[size]} ${className}`}
      disabled={disabled || loading}
      aria-busy={loading}
      {...props}
    >
      {loading ? (
        <span className="flex items-center gap-2">
          <span className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
          {children}
        </span>
      ) : (
        children
      )}
    </button>
  );
};

export interface BadgeProps {
  variant?: 'default' | 'success' | 'warning' | 'error' | 'info';
  children: ReactNode;
  className?: string;
}

const badgeStyles = {
  default: 'bg-slate-100 text-slate-900 ring-1 ring-slate-200 dark:bg-slate-800 dark:text-slate-100 dark:ring-slate-700',
  success: 'bg-green-100 text-green-800 ring-1 ring-green-200 dark:bg-green-900/40 dark:text-green-200 dark:ring-green-900/60',
  warning: 'bg-amber-100 text-amber-800 ring-1 ring-amber-200 dark:bg-amber-900/40 dark:text-amber-200 dark:ring-amber-900/60',
  error: 'bg-red-100 text-red-800 ring-1 ring-red-200 dark:bg-red-900/40 dark:text-red-200 dark:ring-red-900/60',
  info: 'bg-blue-100 text-blue-800 ring-1 ring-blue-200 dark:bg-blue-900/40 dark:text-blue-200 dark:ring-blue-900/60',
};

export const Badge = ({ variant = 'default', children, className = '' }: BadgeProps) => {
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-1 text-xs font-semibold tracking-wide ${badgeStyles[variant]} ${className}`}>
      {children}
    </span>
  );
};
