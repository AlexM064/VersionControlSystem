import { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { Menu as MenuIcon, X as XIcon } from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';
import { appNavigation, NavItem } from './navigationConfig';
import logo from '@/assets/logo.svg';

interface SidebarProps {
  isMobileOpen: boolean;
  onCloseMobile: () => void;
}

export const Sidebar = ({ isMobileOpen, onCloseMobile }: SidebarProps) => {
  const [collapsed, setCollapsed] = useState(false);
  const { user } = useAuth();

  const userRoles = user?.roles ?? [];

  const isAllowed = (item: NavItem) =>
    item.allowedRoles.some((role) => userRoles.includes(role as any));

  const visibleItems = appNavigation.filter(isAllowed);

  return (
    <>
      {/* Desktop Sidebar */}
      <aside
        className={`${collapsed ? 'w-20' : 'w-64'} bg-white dark:bg-slate-900 border-r border-slate-200 dark:border-slate-700 transition-all duration-300 hidden md:flex flex-col`}
      >
        <div className="p-4 border-b border-slate-200 dark:border-slate-700 flex items-start justify-between gap-2">
          <div className="min-w-0">
            {!collapsed ? (
              <>
                <img src={logo} alt="DVCS Logo" className="h-8 w-auto" />
                <p className="mt-1.5 truncate text-[11px] font-medium uppercase tracking-wider text-slate-500 dark:text-slate-400">Document Version Control System</p>
              </>
            ) : (
              <img src={logo} alt="DVCS Logo" className="h-8 w-8 object-cover object-left" />
            )}
          </div>
          <button
            onClick={() => setCollapsed((prev) => !prev)}
            className="p-1 text-slate-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800 rounded transition-colors duration-150"
            aria-label="Toggle sidebar"
          >
            {collapsed ? <MenuIcon size={20} /> : <XIcon size={20} />}
          </button>
        </div>

        <nav className="flex-1 p-4 space-y-2.5">
          {visibleItems.map((item) => (
            <NavLink
              key={item.href}
              to={item.href}
              className={({ isActive }) =>
                `flex items-center gap-3 px-4 py-2.5 rounded-xl transition-all duration-150 ${
                  isActive
                    ? 'bg-blue-100 text-blue-700 shadow-sm dark:bg-blue-900/40 dark:text-blue-200'
                    : 'text-slate-700 hover:bg-slate-100 hover:-translate-y-0.5 dark:text-slate-300 dark:hover:bg-slate-800'
                }`
              }
              onClick={onCloseMobile}
              end={item.href === '/'}
              title={collapsed ? item.label : ''}
            >
              <span className="text-xl">{item.icon}</span>
              {!collapsed && <span className="text-sm font-medium tracking-tight">{item.label}</span>}
            </NavLink>
          ))}
        </nav>

          <div className="p-4 border-t border-slate-200 dark:border-slate-700">
          {!collapsed && (
            <div className="text-xs text-slate-500 dark:text-slate-400 text-center">
              <p className="font-medium">Version 1.0</p>
            </div>
          )}
        </div>
      </aside>

      {/* Mobile Sidebar Drawer */}
      <div
        className={`fixed inset-0 z-40 md:hidden transition-all ${isMobileOpen ? 'opacity-100 visible' : 'opacity-0 invisible'}`}
      >
        <div
          className="absolute inset-0 bg-black/30"
          onClick={onCloseMobile}
          aria-hidden="true"
        />

        <aside className="relative h-full w-64 bg-white dark:bg-slate-900 border-r border-slate-200 dark:border-slate-700 shadow-lg">
          <div className="p-4 border-b border-slate-200 dark:border-slate-700 flex items-center justify-between">
            <div>
              <img src={logo} alt="DVCS Logo" className="h-8 w-auto" />
              <p className="mt-1 text-[11px] font-medium uppercase tracking-wider text-slate-500 dark:text-slate-400">Document Version Control System</p>
            </div>
            <button
              onClick={onCloseMobile}
              className="p-1 text-slate-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800 rounded transition-colors duration-150"
              aria-label="Close navigation"
            >
              <XIcon size={20} />
            </button>
          </div>

          <nav className="p-4 space-y-2.5">
            {visibleItems.map((item) => (
              <NavLink
                key={item.href}
                to={item.href}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-4 py-2.5 rounded-xl transition-all duration-150 ${
                    isActive
                      ? 'bg-blue-100 text-blue-700 shadow-sm dark:bg-blue-900/40 dark:text-blue-200'
                      : 'text-slate-700 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800'
                  }`
                }
                onClick={onCloseMobile}
                end={item.href === '/'}
              >
                <span className="text-xl">{item.icon}</span>
                <span className="text-sm font-medium tracking-tight">{item.label}</span>
              </NavLink>
            ))}
          </nav>
        </aside>
      </div>
    </>
  );
};
