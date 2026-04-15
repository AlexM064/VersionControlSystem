# DVCS Frontend Project Structure

## Directory Overview

### `/src/api/`
API client and endpoint definitions
- `client.ts` - Axios instance with JWT interceptor
- `auth.api.ts` - Authentication endpoints
- `documents.api.ts` - Document CRUD endpoints
- `versions.api.ts` - Version management endpoints
- `workflow.api.ts` - Approval/publish/rollback endpoints
- `admin.api.ts` - Admin endpoints (role management)

### `/src/app/`
Core application configuration
- `providers.tsx` - React Query and context providers
- `router.tsx` - React Router configuration
- `router.config.ts` - Route definitions and guards

### `/src/types/`
TypeScript type definitions (mirrors backend DTOs)
- `auth.types.ts` - Auth-related types
- `document.types.ts` - Document domain types
- `version.types.ts` - Version domain types
- `workflow.types.ts` - Workflow action types
- `common.types.ts` - Shared types
- `index.ts` - Barrel export

### `/src/components/`
Reusable React components

#### `/layout/`
- `AppLayout.tsx` - Main layout wrapper
- `Sidebar.tsx` - Navigation sidebar with role filtering
- `Topbar.tsx` - Header with user info and logout

#### `/guards/`
- `ProtectedRoute.tsx` - Authentication guard
- `RoleGuard.tsx` - Role-based access guard

#### `/ui/`
- `common.tsx` - Alert, Button, Badge components
- `Card.tsx` - Card components (Card, CardHeader, CardBody, CardFooter)
- `DataTable.tsx` - Reusable data table with pagination
- `index.ts` - Barrel export

### `/src/features/`
Feature-specific modules (to be populated)
- Document features
- Version features
- Review workflow features
- Admin features

### `/src/pages/`
Full page components (route components)
- `LoginPage.tsx`
- `RegisterPage.tsx`
- `DashboardPage.tsx`
- `DocumentsPage.tsx`
- `DocumentDetailsPage.tsx`
- `CreateDocumentPage.tsx`
- `EditDocumentPage.tsx`
- `CreateVersionPage.tsx`
- `CompareVersionsPage.tsx`
- `ReviewQueuePage.tsx`
- `AdminUsersPage.tsx`
- `NotFoundPage.tsx`

### `/src/hooks/`
Custom React hooks
- `useLocalStorage.ts` - LocalStorage management
- `usePermission.ts` - Role-based permission checking
- `useErrorHandler.ts` - Error message formatting
- `index.ts` - Barrel export

### `/src/utils/`
Utility functions and constants
- `constants.ts` - API config, routes, roles, statuses, permissions
- `validators.ts` - Zod schemas for form validation
- `helpers.ts` - Date, text, diff utilities
- `ui-helpers.ts` - Avatar, email formatting utilities

### Root Configuration Files
- `vite.config.ts` - Vite build configuration
- `tsconfig.json` - TypeScript configuration
- `tailwind.config.js` - Tailwind CSS customization
- `postcss.config.js` - PostCSS configuration
- `package.json` - Dependencies and scripts
- `.env.example` - Environment variables template
- `.gitignore` - Git ignore rules
- `.eslintrc.cjs` - ESLint configuration
- `index.html` - HTML entry point

## Data Flow

```
Pages
  ↓
Components (UI)
  ↓
Hooks (React Query, Custom)
  ↓
API Client (axios)
  ↓
Backend
```

## Key Implementation Notes

1. **Authentication**: Token stored in localStorage, auto-refresh on boot
2. **API Caching**: React Query with 5min stale time, 10min cache time
3. **Error Handling**: Centralized 401 interception, user feedback via Alert component
4. **Type Safety**: 100% TypeScript coverage, mirrored backend DTOs
5. **Styling**: Tailwind CSS utility-first approach
6. **State Management**: Combination of localStorage, React Query, React Context

## Adding New Features

### Adding a New Page
1. Create component in `/src/pages/`
2. Add type definitions in `/src/types/`
3. Add route to `/src/app/router.tsx`
4. Update navigation in `/src/components/layout/Sidebar.tsx`

### Adding a New API Endpoint
1. Create method in appropriate `/src/api/*.api.ts` file
2. Define request/response types in `/src/types/`
3. Create hook for data fetching using React Query
4. Use in components

### Adding a New Reusable Component
1. Create in `/src/components/ui/` or feature-specific subdirectory
2. Export from `/src/components/*/index.ts`
3. Document props interface

## Development Commands

```bash
npm run dev           # Start dev server on http://localhost:3000
npm run build         # Build for production
npm run preview       # Preview production build
npm run lint          # Run ESLint
npm run type-check    # Run TypeScript compiler
```

## Environment Setup

Create `.env.local` (not committed):
```
VITE_API_URL=http://localhost:8080/api
```

## Next Steps

1. Install dependencies: `npm install`
2. Configure Vite dev server to proxy to backend
3. Implement authentication flow (login/register forms)
4. Create React Query hooks for data fetching
5. Implement role-based UI rendering
6. Build feature pages incrementally
