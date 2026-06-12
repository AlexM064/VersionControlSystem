import { useCallback } from 'react';
import { STORAGE_KEYS } from '@/utils/constants';

export const useLocalStorage = <T>(key: string, defaultValue: T) => {
  const getStoredValue = useCallback((): T => {
    try {
      const item = localStorage.getItem(`${STORAGE_KEYS.TOKEN}:${key}`);
      return item ? JSON.parse(item) : defaultValue;
    } catch {
      return defaultValue;
    }
  }, [key, defaultValue]);

  const setValue = useCallback(
    (value: T) => {
      try {
        localStorage.setItem(`${STORAGE_KEYS.TOKEN}:${key}`, JSON.stringify(value));
      } catch (error) {
        console.error(`Failed to save ${key} to localStorage:`, error);
      }
    },
    [key]
  );

  const removeValue = useCallback(() => {
    try {
      localStorage.removeItem(`${STORAGE_KEYS.TOKEN}:${key}`);
    } catch (error) {
      console.error(`Failed to remove ${key} from localStorage:`, error);
    }
  }, [key]);

  return [getStoredValue(), setValue, removeValue] as const;
};
