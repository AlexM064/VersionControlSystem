import { ReactNode } from 'react';
import { QueryClientProvider, QueryClient } from '@tanstack/react-query';
import { AuthProvider } from '@/contexts/AuthContext';
import { ThemeProvider } from '@/contexts/ThemeContext';
import { ToastProvider } from '@/contexts/ToastContext';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5, // 5 minutes
      gcTime: 1000 * 60 * 10, // 10 minutes (formerly cacheTime)
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

export interface ProvidersProps {
  children: ReactNode;
}

export const Providers = ({ children }: ProvidersProps) => {
  return (
    <ThemeProvider>
      <AuthProvider>
        <ToastProvider>
          <QueryClientProvider client={queryClient}>
            {children}
          </QueryClientProvider>
        </ToastProvider>
      </AuthProvider>
    </ThemeProvider>
  );
};
