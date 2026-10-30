import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { t } from '../../i18n';

export const Sidebar = () => {
  const { user, hasAnyRole } = useAuth();

  const isStaff = hasAnyRole([
    'SUPER_ADMIN',
    'MUNICIPAL_ADMIN',
    'DEPARTMENT_HEAD',
    'WARD_OFFICER',
    'SUPERVISOR',
    'FIELD_WORKER',
  ]);

  const navItems = [
    { label: 'Citizen Home', path: '/', show: true, icon: '🏠' },
    {
      label: 'Operations Board',
      path: '/operations-dashboard',
      show: isStaff,
      icon: '📊',
    },
    { label: 'My Grievances', path: '/citizen/grievances', show: true, icon: '📑' },
    { label: 'File Grievance', path: '/complaints/new', show: true, icon: '✍️' },
    { label: 'Ward Incident Map', path: '/map', show: true, icon: '🗺️' },
    { label: 'Track by ID', path: '/track', show: true, icon: '🔍' },
    {
      label: 'All Complaints',
      path: '/complaints',
      show: isStaff,
      icon: '📂',
    },
    {
      label: 'Wards Admin',
      path: '/wards',
      show: hasAnyRole(['SUPER_ADMIN', 'MUNICIPAL_ADMIN', 'DEPARTMENT_HEAD']),
      icon: '🏛️',
    },
    {
      label: 'Departments',
      path: '/departments',
      show: hasAnyRole(['SUPER_ADMIN', 'MUNICIPAL_ADMIN']),
      icon: '🏢',
    },
  ];

  return (
    <aside className="w-60 bg-slate-900 text-slate-300 min-h-[calc(100vh-4rem)] flex flex-col p-4 border-r border-slate-800 shadow-md">
      <div className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider mb-3 px-3">
        Citizen & Operations Menu
      </div>
      <nav className="flex flex-col gap-1 flex-1" aria-label="Main Navigation">
        {navItems
          .filter((item) => item.show)
          .map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              end={item.path === '/'}
              className={({ isActive }) =>
                `flex items-center gap-2.5 px-3 py-2 rounded-lg text-xs font-medium transition-colors ${
                  isActive
                    ? 'bg-blue-600 text-white font-semibold shadow-sm'
                    : 'text-slate-300 hover:bg-slate-800 hover:text-white'
                }`
              }
            >
              <span className="text-sm" aria-hidden="true">{item.icon}</span>
              <span>{item.label}</span>
            </NavLink>
          ))}
      </nav>

      {user && (
        <div className="pt-4 border-t border-slate-800 text-[11px] text-slate-400">
          <div className="text-[10px] uppercase font-bold text-slate-500">Authenticated</div>
          <div className="font-semibold text-white truncate mt-0.5">{user.fullName || user.username}</div>
          <div className="text-[10px] text-blue-400 mt-0.5 truncate">
            {user.roles?.map((r) => r.replace('ROLE_', '')).join(', ') || 'CITIZEN'}
          </div>
        </div>
      )}
    </aside>
  );
};

export default Sidebar;
