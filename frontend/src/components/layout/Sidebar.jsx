import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth.js';
import useTranslation from '../../hooks/useTranslation.js';

export const Sidebar = () => {
  const { user, hasAnyRole } = useAuth();
  const { t } = useTranslation();

  const isStaff = hasAnyRole([
    'SUPER_ADMIN',
    'MUNICIPAL_ADMIN',
    'DEPARTMENT_HEAD',
    'WARD_OFFICER',
    'SUPERVISOR',
    'FIELD_WORKER',
    'OFFICER',
    'OPERATOR',
  ]);

  const navItems = [
    { label: t('nav.dashboard', 'Citizen Home'), path: '/', show: true, icon: '🏠' },
    {
      label: t('nav.operations', 'Operations Board'),
      path: '/operations-dashboard',
      show: isStaff,
      icon: '📊',
    },
    { label: t('nav.complaints', 'My Grievances'), path: '/citizen/grievances', show: true, icon: '📑' },
    { label: t('nav.newComplaint', 'File Grievance'), path: '/complaints/new', show: true, icon: '✍️' },
    { label: t('nav.map', 'Ward Incident Map'), path: '/map', show: true, icon: '🗺️' },
    { label: t('nav.transparency', 'Public Transparency'), path: '/transparency', show: true, icon: '🔓' },
    { label: t('nav.track', 'Track by ID'), path: '/track', show: true, icon: '🔍' },
    {
      label: 'All Complaints',
      path: '/complaints',
      show: isStaff,
      icon: '📂',
    },
    {
      label: t('nav.wards', 'Wards Admin'),
      path: '/wards',
      show: hasAnyRole(['SUPER_ADMIN', 'MUNICIPAL_ADMIN', 'DEPARTMENT_HEAD']),
      icon: '🏛️',
    },
    {
      label: t('nav.departments', 'Departments'),
      path: '/departments',
      show: hasAnyRole(['SUPER_ADMIN', 'MUNICIPAL_ADMIN']),
      icon: '🏢',
    },
  ];

  return (
    <aside aria-label="Sidebar navigation" className="w-60 bg-slate-900 text-slate-300 min-h-[calc(100vh-4rem)] flex flex-col p-4 border-r border-slate-800 shadow-md hidden md:flex">
      <div className="text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-3 px-3">
        Municipal Platform
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
                `flex items-center gap-2.5 px-3 py-2 rounded-xl text-xs font-semibold transition-colors ${
                  isActive
                    ? 'bg-blue-600 text-white shadow-xs'
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
