import axios, { AxiosInstance, AxiosError } from 'axios';
import { API_BASE_URL, API_TIMEOUT, STORAGE_KEYS } from '@/utils/constants';
import { getApiErrorMessage } from './error';

export const createApiClient = (): AxiosInstance => {
  const client = axios.create({
    baseURL: API_BASE_URL,
    timeout: API_TIMEOUT,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  // Request interceptor
  client.interceptors.request.use(
    (config) => {
      // Token can be set via AuthContext
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
  );

  // Response interceptor - handle 401 Unauthorized
  client.interceptors.response.use(
    (response) => response,
    (error: AxiosError) => {
      const requestUrl = error.config?.url || '';
      const isAuthRequest = requestUrl.includes('/auth/login') || requestUrl.includes('/auth/register');

      if (error.response?.status === 401 && !isAuthRequest) {
        // Clear auth data from localStorage
        localStorage.removeItem(STORAGE_KEYS.TOKEN);
        localStorage.removeItem(STORAGE_KEYS.USER);
        // Redirect to login
        window.location.href = '/login';
      }

      error.message = getApiErrorMessage(error);

      return Promise.reject(error);
    }
  );

  return client;
};

export const apiClient = createApiClient();
