import { ROUTES, USER_ROLES } from '@/utils/constants';

export interface NavItem {
  label: string;
  href: string;
  icon: string;
  allowedRoles: Array<keyof typeof USER_ROLES>;
}

export const appNavigation: NavItem[] = [
  {
    label: 'Dashboard',
    href: ROUTES.ROOT,
    icon: '📊',
    allowedRoles: ['ADMIN', 'AUTHOR', 'REVIEWER', 'READER'],
  },
  {
    label: 'Documents',
    href: ROUTES.DOCUMENTS,
    icon: '📄',
    allowedRoles: ['ADMIN', 'AUTHOR', 'REVIEWER', 'READER'],
  },
  {
    label: 'Review Queue',
    href: ROUTES.REVIEW_QUEUE,
    icon: '✅',
    allowedRoles: ['ADMIN', 'REVIEWER'],
  },
  {
    label: 'Admin',
    href: ROUTES.ADMIN_USERS,
    icon: '⚙️',
    allowedRoles: ['ADMIN'],
  },
];
