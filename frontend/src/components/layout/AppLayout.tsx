import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Topbar } from './Topbar';

export const AppLayout = () => {
  const [mobileNavOpen, setMobileNavOpen] = useState(false);

  const handleToggleMobileNav = () => setMobileNavOpen((prev) => !prev);
  const handleCloseMobileNav = () => setMobileNavOpen(false);

  return (
    <div className="flex h-screen bg-slate-100 dark:bg-slate-950 overflow-hidden">
      {/* Sidebar */}
      <Sidebar isMobileOpen={mobileNavOpen} onCloseMobile={handleCloseMobileNav} />

      {/* Main Content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Topbar */}
        <Topbar onToggleMobileNav={handleToggleMobileNav} />

        {/* Page Content */}
        <main className="flex-1 overflow-auto p-4 md:p-6 bg-slate-100 dark:bg-slate-950">
          <Outlet />
        </main>
      </div>
    </div>
  );
};
