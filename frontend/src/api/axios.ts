import axios, { AxiosInstance, AxiosError } from 'axios';

export const createApiClient = (): AxiosInstance => {
  const client = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 60000, // 60 seconds
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
      if (error.response?.status === 401) {
        // Clear auth data from localStorage
        localStorage.removeItem('dvcs_token');
        localStorage.removeItem('dvcs_user');
        // Redirect to login
        window.location.href = '/login';
      }
      return Promise.reject(error);
    }
  );

  return client;
};

export const apiClient = createApiClient();
