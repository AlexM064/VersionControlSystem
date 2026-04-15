import { apiClient } from './axios';
import { UserRole } from '../types/auth';

export const usersApi = {
  changeUserRole: async (userId: number, newRole: UserRole) => {
    const response = await apiClient.patch(`/users/${userId}/role`, {
      role: newRole,
    });
    return response.data;
  },
};
