import { createBrowserRouter } from 'react-router-dom';
import { ROUTES } from '@/utils/constants';
import { AppLayout } from '@/components/layout/AppLayout';
import { LoginPage } from '@/pages/LoginPage';
import { RegisterPage } from '@/pages/RegisterPage';
import { DashboardPage } from '@/pages/DashboardPage';
import { DocumentsPage } from '@/pages/DocumentsPage';
import { DocumentDetailsPage } from '@/pages/DocumentDetailsPage';
import { CreateDocumentPage } from '@/pages/CreateDocumentPage';
import { EditDocumentPage } from '@/pages/EditDocumentPage';
import { CreateVersionPage } from '@/pages/CreateVersionPage';
import { CompareVersionsPage } from '@/pages/CompareVersionsPage';
import { ReviewQueuePage } from '@/pages/ReviewQueuePage';
import { AdminUsersPage } from '@/pages/AdminUsersPage';
import { NotFoundPage } from '@/pages/NotFoundPage';
import { ProtectedRoute } from './router.config.tsx';
import { UserRole } from '@/types/auth';

export const router = createBrowserRouter([
  {
    path: ROUTES.LOGIN,
    element: <LoginPage />,
  },
  {
    path: ROUTES.REGISTER,
    element: <RegisterPage />,
  },
  {
    path: ROUTES.ROOT,
    element: (
      <ProtectedRoute>
        <AppLayout />
      </ProtectedRoute>
    ),
    children: [
      {
        index: true,
        element: <DashboardPage />,
      },
      {
        path: ROUTES.DASHBOARD,
        element: <DashboardPage />,
      },
      {
        path: ROUTES.DOCUMENTS,
        element: <DocumentsPage />,
      },
      {
        path: ROUTES.CREATE_DOCUMENT,
        element: (
          <ProtectedRoute requiredRoles={[UserRole.ADMIN, UserRole.AUTHOR]}>
            <CreateDocumentPage />
          </ProtectedRoute>
        ),
      },
      {
        path: ROUTES.DOCUMENT_DETAIL,
        element: <DocumentDetailsPage />,
      },
      {
        path: ROUTES.DOCUMENT_EDIT,
        element: (
          <ProtectedRoute requiredRoles={[UserRole.ADMIN, UserRole.AUTHOR]}>
            <EditDocumentPage />
          </ProtectedRoute>
        ),
      },
      {
        path: ROUTES.CREATE_VERSION,
        element: (
          <ProtectedRoute requiredRoles={[UserRole.ADMIN, UserRole.AUTHOR]}>
            <CreateVersionPage />
          </ProtectedRoute>
        ),
      },
      {
        path: ROUTES.COMPARE_VERSIONS,
        element: (
          <ProtectedRoute requiredRoles={[UserRole.ADMIN, UserRole.AUTHOR, UserRole.REVIEWER]}>
            <CompareVersionsPage />
          </ProtectedRoute>
        ),
      },
      {
        path: ROUTES.REVIEW_QUEUE,
        element: (
          <ProtectedRoute requiredRoles={[UserRole.ADMIN, UserRole.REVIEWER]}>
            <ReviewQueuePage />
          </ProtectedRoute>
        ),
      },
      {
        path: ROUTES.ADMIN_USERS,
        element: (
          <ProtectedRoute requiredRoles={[UserRole.ADMIN]}>
            <AdminUsersPage />
          </ProtectedRoute>
        ),
      },
    ],
  },
  {
    path: ROUTES.NOT_FOUND,
    element: <NotFoundPage />,
  },
]);
