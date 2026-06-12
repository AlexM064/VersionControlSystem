import { useCallback } from 'react';
import { ROLE_PERMISSIONS } from '@/utils/constants';
import { useAuth } from '@/contexts/AuthContext';

export const usePermission = () => {
  const { user } = useAuth();

  const hasPermission = useCallback(
    (permission: string): boolean => {
      if (!user?.roles || user.roles.length === 0) return false;
      
      // Check if any of user's roles have permission
      return user.roles.some((role) => {
        const rolePerms = ROLE_PERMISSIONS[role as keyof typeof ROLE_PERMISSIONS];
        return rolePerms?.[permission as keyof typeof rolePerms] || false;
      });
    },
    [user?.roles]
  );

  const can = useCallback(
    (permission: string): boolean => hasPermission(permission),
    [hasPermission]
  );

  const cannot = useCallback(
    (permission: string): boolean => !hasPermission(permission),
    [hasPermission]
  );

  return { hasPermission, can, cannot };
};
