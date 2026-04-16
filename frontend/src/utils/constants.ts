// API Configuration
const rawApiBaseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const API_BASE_URL = rawApiBaseUrl.replace(/\/api\/?$/, '');
export const API_TIMEOUT = 60000; // 60 seconds

// Local Storage Keys
export const STORAGE_KEYS = {
  TOKEN: 'dvcs_token',
  USER: 'dvcs_user',
  THEME: 'dvcs_theme',
};

// Route Paths
export const ROUTES = {
  ROOT: '/',
  LOGIN: '/login',
  REGISTER: '/register',
  DASHBOARD: '/dashboard',
  DOCUMENTS: '/documents',
  DOCUMENT_DETAIL: '/documents/:id',
  DOCUMENT_EDIT: '/documents/:id/edit',
  CREATE_DOCUMENT: '/documents/create',
  CREATE_VERSION: '/documents/:id/versions/create',
  COMPARE_VERSIONS: '/documents/:id/compare',
  REVIEW_QUEUE: '/review-queue',
  ADMIN_USERS: '/admin/users',
  NOT_FOUND: '*',
};

// User Roles
export const USER_ROLES = {
  ADMIN: 'ADMIN',
  AUTHOR: 'AUTHOR',
  REVIEWER: 'REVIEWER',
  READER: 'READER',
};

// Document Statuses
export const DOCUMENT_STATUSES = {
  ACTIVE: 'ACTIVE',
  ARCHIVED: 'ARCHIVED',
};

// Version Statuses
export const VERSION_STATUSES = {
  DRAFT: 'DRAFT',
  IN_REVIEW: 'IN_REVIEW',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED',
  PUBLISHED: 'PUBLISHED',
};

// Permission Matrix
export const ROLE_PERMISSIONS = {
  ADMIN: {
    CREATE_DOCUMENT: true,
    EDIT_DOCUMENT: true,
    DELETE_DOCUMENT: true,
    ARCHIVE_DOCUMENT: true,
    CREATE_VERSION: true,
    APPROVE_VERSION: true,
    REJECT_VERSION: true,
    PUBLISH_VERSION: true,
    ROLLBACK_VERSION: true,
    MANAGE_USERS: true,
    VIEW_DOCUMENTS: true,
  },
  AUTHOR: {
    CREATE_DOCUMENT: true,
    EDIT_DOCUMENT: true,
    DELETE_DOCUMENT: false,
    ARCHIVE_DOCUMENT: false,
    CREATE_VERSION: true,
    APPROVE_VERSION: false,
    REJECT_VERSION: false,
    PUBLISH_VERSION: false,
    ROLLBACK_VERSION: false,
    MANAGE_USERS: false,
    VIEW_DOCUMENTS: true,
  },
  REVIEWER: {
    CREATE_DOCUMENT: false,
    EDIT_DOCUMENT: false,
    DELETE_DOCUMENT: false,
    ARCHIVE_DOCUMENT: false,
    CREATE_VERSION: false,
    APPROVE_VERSION: true,
    REJECT_VERSION: true,
    PUBLISH_VERSION: false,
    ROLLBACK_VERSION: false,
    MANAGE_USERS: false,
    VIEW_DOCUMENTS: true,
  },
  READER: {
    CREATE_DOCUMENT: false,
    EDIT_DOCUMENT: false,
    DELETE_DOCUMENT: false,
    ARCHIVE_DOCUMENT: false,
    CREATE_VERSION: false,
    APPROVE_VERSION: false,
    REJECT_VERSION: false,
    PUBLISH_VERSION: false,
    ROLLBACK_VERSION: false,
    MANAGE_USERS: false,
    VIEW_DOCUMENTS: true,
  },
};
