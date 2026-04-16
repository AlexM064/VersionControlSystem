import { useCallback } from 'react';
import { getApiErrorMessage } from '@/api';

export const useErrorHandler = () => {
  const getErrorMessage = useCallback((error: unknown): string => {
    return getApiErrorMessage(error);
  }, []);

  return { getErrorMessage };
};
