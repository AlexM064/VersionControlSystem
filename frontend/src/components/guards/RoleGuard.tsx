import { ReactNode } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { UserRole } from '@/types/auth';

export interface RoleGuardProps {
  children: ReactNode;
  requiredRoles: UserRole[];
  fallback?: ReactNode;
}

export const RoleGuard = ({ children, requiredRoles, fallback = null }: RoleGuardProps) => {
  const { user } = useAuth();

  if (!user) {
    return <>{fallback}</>;
  }

  const hasRequiredRole = requiredRoles.some((role) => user.roles.includes(role));

  if (!hasRequiredRole) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
};
