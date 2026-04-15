import { AxiosError } from 'axios';
import { useCallback } from 'react';

export const useErrorHandler = () => {
  const getErrorMessage = useCallback((error: unknown): string => {
    if (error instanceof AxiosError) {
      if (error.response?.status === 404) {
        return 'Resource not found';
      }
      if (error.response?.status === 401) {
        return 'Unauthorized - please login again';
      }
      if (error.response?.status === 403) {
        return 'You do not have permission to perform this action';
      }
      if (error.response?.status === 400) {
        return (error.response?.data as any)?.message || 'Invalid request';
      }
      if (error.response?.status === 500) {
        return 'Server error - please try again later';
      }
      return error.message || 'An error occurred';
    }
    if (error instanceof Error) {
      return error.message;
    }
    return 'An unknown error occurred';
  }, []);

  return { getErrorMessage };
};
