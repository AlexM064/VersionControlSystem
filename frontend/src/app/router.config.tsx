import { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { ROUTES } from '@/utils/constants';
import { UserRole } from '@/types/auth';

export interface ProtectedRouteProps {
  children: ReactNode;
  requiredRoles?: UserRole[];
  fallbackPath?: string;
}

export const ProtectedRoute = ({
  children,
  requiredRoles,
  fallbackPath = ROUTES.LOGIN,
}: ProtectedRouteProps) => {
  const { isAuthenticated, isLoading, user } = useAuth();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to={fallbackPath} replace />;
  }

  if (requiredRoles && user) {
    const hasRequiredRole = requiredRoles.some((role) => user.roles.includes(role));
    if (!hasRequiredRole) {
      return <Navigate to={ROUTES.ROOT} replace />;
    }
  }

  return <>{children}</>;
};

