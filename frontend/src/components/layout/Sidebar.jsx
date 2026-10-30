import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { t } from '../../i18n';

export const Sidebar = () => {
  const { user, hasRole } = useAuth();

  const navItems = [
    { label: t('nav.dashboard'), path: '/', show: true },
    {
      label: 'Field Operations',
      path: '/operations',
      show:
        hasRole('FIELD_WORKER') ||
        hasRole('SUPERVISOR') ||
        hasRole('DEPT_ADMIN') ||
        hasRole('SUPER_ADMIN') ||
        hasRole('ADMIN') ||
        hasRole('EMPLOYEE'),
    },
    { label: t('nav.complaints'), path: '/complaints', show: true },
    { label: t('nav.newComplaint'), path: '/complaints/new', show: true },
    { label: t('nav.wards'), path: '/wards', show: hasRole('SUPER_ADMIN') || hasRole('DEPT_ADMIN') },
    { label: t('nav.departments'), path: '/departments', show: hasRole('SUPER_ADMIN') },
  ];

  return (
    <aside className="w-56 bg-slate-900 text-slate-300 min-h-[calc(100vh-4rem)] flex flex-col p-4 border-r border-slate-800">
      <div className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider mb-3 px-3">
        Operations Menu
      </div>
      <nav className="flex flex-col gap-1 flex-1">
        {navItems
          .filter((item) => item.show)
          .map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              end={item.path === '/'}
              className={({ isActive }) =>
                `px-3 py-2 rounded text-xs font-medium transition-colors ${
                  isActive
                    ? 'bg-blue-600 text-white font-semibold'
                    : 'text-slate-300 hover:bg-slate-800 hover:text-white'
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
      </nav>

      {user && (
        <div className="pt-4 border-t border-slate-800 text-[11px] text-slate-400">
          <div>Logged in as:</div>
          <div className="font-semibold text-white truncate">{user.username}</div>
          <div className="text-[10px] text-blue-400 mt-0.5">{user.roles?.join(', ')}</div>
        </div>
      )}
    </aside>
  );
};

export default Sidebar;
