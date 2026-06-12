import { Menu as MenuIcon, User as UserIcon, LogOut as LogOutIcon, Sun as SunIcon, Moon as MoonIcon } from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';
import { useTheme } from '@/contexts/ThemeContext';

interface TopbarProps {
  onToggleMobileNav: () => void;
}

export const Topbar = ({ onToggleMobileNav }: TopbarProps) => {
  const { user, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();

  return (
    <header className="h-16 bg-white/95 backdrop-blur-sm border-b border-slate-200 dark:bg-slate-900/95 dark:border-slate-700 flex items-center justify-between px-4 md:px-6 shadow-sm">
      <div className="flex items-center gap-3 min-w-0">
        <button
          onClick={onToggleMobileNav}
          className="p-2 rounded-md text-slate-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800 transition-colors duration-150 md:hidden"
          aria-label="Open navigation"
        >
          <MenuIcon size={20} />
        </button>

        <div className="min-w-0">
          <h2 className="text-lg font-semibold tracking-tight text-slate-900 dark:text-slate-50">Document Version Control</h2>
          <p className="text-xs leading-5 text-slate-500 dark:text-slate-400">Welcome back{user?.username ? `, ${user.username}` : ''}</p>
        </div>
      </div>

      <div className="flex items-center gap-3">
        <div className="hidden sm:flex items-center gap-2.5 px-3 py-1.5 rounded-xl border border-slate-200 bg-slate-50 dark:bg-slate-800 dark:border-slate-700">
          <div className="w-8 h-8 rounded-full bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center">
            <UserIcon size={16} className="text-white" />
          </div>
          <div className="text-xs text-slate-700 dark:text-slate-300">
            <p className="font-medium leading-none">{user?.username?.trim() || 'User'}</p>
            <p className="text-[10px] leading-none uppercase tracking-wider text-slate-500 dark:text-slate-400">{user?.roles.join(', ')}</p>
          </div>
        </div>

        <button
          onClick={toggleTheme}
          className="rounded-xl p-2 text-slate-600 transition-all duration-150 hover:bg-slate-100 hover:-translate-y-0.5 dark:text-slate-300 dark:hover:bg-slate-800"
          title={theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
          aria-label={theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
        >
          {theme === 'dark' ? <SunIcon size={18} /> : <MoonIcon size={18} />}
        </button>

        <button
          onClick={logout}
          className="rounded-xl p-2 text-slate-600 transition-all duration-150 hover:bg-red-50 hover:text-red-600 hover:-translate-y-0.5 dark:text-slate-300 dark:hover:bg-red-950/40 dark:hover:text-red-300"
          title="Logout"
        >
          <LogOutIcon size={18} />
        </button>
      </div>
    </header>
  );
};
