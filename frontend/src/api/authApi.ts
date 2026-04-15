import { apiClient } from './axios';
import { AuthResponse, LoginRequest, RegisterRequest, User } from '../types/auth';

export const authApi = {
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await apiClient.post('/auth/login', credentials);
    return response.data;
  },

  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await apiClient.post('/auth/register', data);
    return response.data;
  },

  getCurrentUser: async (): Promise<User> => {
    const response = await apiClient.get('/auth/me');
    return response.data;
  },

  logout: async (): Promise<void> => {
    // Logout is typically handled client-side by clearing token
    // If backend has logout endpoint, call it here
  },
};
