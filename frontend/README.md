# DVCS Frontend

React + TypeScript + Vite frontend for the Document Version Control System.

## Project Structure

```
src/
├── api/                    # API client and endpoints
├── app/                    # App configuration (providers, router)
├── components/
│   ├── layout/            # Layout components (AppLayout, Sidebar, Topbar)
│   ├── guards/            # Route guards (ProtectedRoute, RoleGuard)
│   └── ui/                # UI components
├── features/              # Feature-specific modules
├── pages/                 # Page components
├── types/                 # TypeScript type definitions
├── hooks/                 # Custom hooks
├── utils/                 # Utility functions
├── App.tsx                # Root component
└── main.tsx               # Entry point
```

## Setup

### Prerequisites
- Node.js 18+
- npm or yarn or pnpm

### Installation

```bash
cd frontend
npm install
```

### Development

```bash
npm run dev
```

The app will open at `http://localhost:3000`

### Build

```bash
npm run build
```

### Type Check

```bash
npm run type-check
```

## Tech Stack

- **React 18** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool
- **React Router** - Routing
- **Tailwind CSS** - Styling
- **Axios** - HTTP client
- **React Query (TanStack Query)** - Data fetching & caching
- **React Hook Form** - Form management
- **Zod** - Schema validation
- **Lucide React** - Icons

## Features

### Authentication
- Login / Register
- JWT token management
- Auto-logout on token expiration

### Document Management
- Create, read, update, archive documents
- Document history tracking
- Version comparison

### Version Control
- Create document versions
- Submit for review workflow
- Approve/reject versions
- Publish/rollback

### Role-Based Access Control
- ADMIN: Full access
- AUTHOR: Create/edit documents and versions
- REVIEWER: Approve/reject versions
- READER: View only

### Admin Panel
- User management
- Role assignment

## API Integration

All API calls go through the centralized API client in `src/api/client.ts`. The client includes:
- Automatic JWT token injection
- Error handling and 401 response interception
- Base URL configuration

### API Endpoints

See backend documentation for full endpoint specifications. The frontend respects:
- POST /auth/login
- POST /auth/register
- GET /auth/me
- GET /documents
- POST /documents
- PUT /documents/{id}
- PATCH /documents/{id}/archive
- GET /documents/{id}/history
- POST /documents/{id}/versions
- GET /documents/{id}/versions
- POST /versions/{id}/approve
- POST /versions/{id}/reject
- POST /versions/{id}/publish
- POST /versions/{id}/rollback
- PATCH /users/{id}/role

## Environment Variables

Create a `.env` file based on `.env.example`:

```
VITE_API_URL=http://localhost:8080/api
```

## State Management

We use a combination of:
- **localStorage** - Token persistence
- **React Query** - Server state management (data fetching, caching)
- **React Context** - For authentication state (to be enhanced)

## Type Safety

All backend DTOs are mirrored in `src/types/` for full type safety across the application.

## Development Guidelines

1. **Components**: Keep components focused and reusable
2. **API**: All API calls through `src/api/` modules
3. **Types**: Define types in `src/types/` organized by domain
4. **Hooks**: Extract complex logic into custom hooks
5. **Utils**: Utility functions in `src/utils/`
6. **Styling**: Use Tailwind CSS utility classes

## Common Tasks

### Adding a new page
1. Create component in `src/pages/`
2. Add route to `src/app/router.tsx`
3. Link from navigation if needed

### Adding a new feature
1. Create API methods in `src/api/`
2. Create types in `src/types/`
3. Create components in `src/components/`
4. Use React Query for data fetching
5. Add to page/feature

### Adding a new API endpoint
1. Create new method in appropriate `src/api/*.api.ts` file
2. Define required types in `src/types/`
3. Use in components via React Query hooks

## Notes

- The backend is the source of truth
- Do not invent endpoints or mock logic
- Treat DTOs as contracts
- Use strong typing everywhere
