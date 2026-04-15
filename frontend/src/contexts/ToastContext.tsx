import { createContext, ReactNode, useCallback, useContext, useMemo, useState } from 'react';
import { AlertCircle, CheckCircle2, Info, X } from 'lucide-react';

type ToastType = 'success' | 'error' | 'info';

interface ToastItem {
  id: number;
  type: ToastType;
  title?: string;
  message: string;
}

interface AddToastInput {
  type: ToastType;
  title?: string;
  message: string;
  duration?: number;
}

interface ToastContextValue {
  addToast: (input: AddToastInput) => void;
  success: (message: string, title?: string) => void;
  error: (message: string, title?: string) => void;
  info: (message: string, title?: string) => void;
}

const ToastContext = createContext<ToastContextValue | undefined>(undefined);

const toastStyles: Record<ToastType, { box: string; title: string; message: string; icon: ReactNode }> = {
  success: {
    box: 'border-green-200/80 bg-white dark:border-green-900/60 dark:bg-slate-900',
    title: 'text-green-800 dark:text-green-300',
    message: 'text-slate-700 dark:text-slate-200',
    icon: <CheckCircle2 size={18} className="text-green-600 dark:text-green-400" />,
  },
  error: {
    box: 'border-red-200/80 bg-white dark:border-red-900/60 dark:bg-slate-900',
    title: 'text-red-800 dark:text-red-300',
    message: 'text-slate-700 dark:text-slate-200',
    icon: <AlertCircle size={18} className="text-red-600 dark:text-red-400" />,
  },
  info: {
    box: 'border-blue-200/80 bg-white dark:border-blue-900/60 dark:bg-slate-900',
    title: 'text-blue-800 dark:text-blue-300',
    message: 'text-slate-700 dark:text-slate-200',
    icon: <Info size={18} className="text-blue-600 dark:text-blue-400" />,
  },
};

interface ToastViewportProps {
  toasts: ToastItem[];
  onRemove: (id: number) => void;
}

const ToastViewport = ({ toasts, onRemove }: ToastViewportProps) => {
  return (
    <div className="pointer-events-none fixed right-4 top-4 z-[100] flex w-[calc(100%-2rem)] max-w-sm flex-col gap-2">
      {toasts.map((toast) => {
        const styles = toastStyles[toast.type];

        return (
          <div
            key={toast.id}
            className={`pointer-events-auto rounded-lg border p-3 shadow-lg transition-all duration-200 ${styles.box}`}
            role="status"
            aria-live="polite"
          >
            <div className="flex items-start gap-2">
              <div className="mt-0.5 shrink-0">{styles.icon}</div>
              <div className="min-w-0 flex-1">
                {toast.title && <p className={`text-sm font-semibold ${styles.title}`}>{toast.title}</p>}
                <p className={`text-sm leading-5 ${styles.message}`}>{toast.message}</p>
              </div>
              <button
                type="button"
                className="shrink-0 rounded p-1 text-slate-400 transition-colors duration-150 hover:bg-slate-100 hover:text-slate-600 dark:hover:bg-slate-800 dark:hover:text-slate-300"
                onClick={() => onRemove(toast.id)}
                aria-label="Dismiss notification"
              >
                <X size={14} />
              </button>
            </div>
          </div>
        );
      })}
    </div>
  );
};

export const ToastProvider = ({ children }: { children: ReactNode }) => {
  const [toasts, setToasts] = useState<ToastItem[]>([]);

  const removeToast = useCallback((id: number) => {
    setToasts((current) => current.filter((toast) => toast.id !== id));
  }, []);

  const addToast = useCallback(
    ({ type, title, message, duration = 3500 }: AddToastInput) => {
      const id = Date.now() + Math.floor(Math.random() * 1000);

      setToasts((current) => [...current, { id, type, title, message }]);

      window.setTimeout(() => {
        removeToast(id);
      }, duration);
    },
    [removeToast]
  );

  const value = useMemo<ToastContextValue>(
    () => ({
      addToast,
      success: (message, title = 'Success') => addToast({ type: 'success', title, message }),
      error: (message, title = 'Something went wrong') => addToast({ type: 'error', title, message }),
      info: (message, title = 'Info') => addToast({ type: 'info', title, message }),
    }),
    [addToast]
  );

  return (
    <ToastContext.Provider value={value}>
      {children}
      <ToastViewport toasts={toasts} onRemove={removeToast} />
    </ToastContext.Provider>
  );
};

export const useToast = () => {
  const context = useContext(ToastContext);

  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }

  return context;
};