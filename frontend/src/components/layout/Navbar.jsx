import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth.js';
import useTranslation from '../../hooks/useTranslation.js';

export const Navbar = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const { t, currentLang, changeLanguage, supportedLanguages } = useTranslation();

  return (
    <header className="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-4 sm:px-6 sticky top-0 z-30 shadow-xs">
      <div className="flex items-center gap-3">
        <Link to="/" className="flex items-center gap-2">
          <div className="h-8 w-8 rounded-xl bg-blue-600 text-white font-black flex items-center justify-center text-sm shadow-xs">
            CF
          </div>
          <div>
            <h1 className="text-base font-extrabold text-slate-900 leading-tight">
              {t('app.title', 'CivicFix')}
            </h1>
            <p className="text-[10px] text-slate-500 hidden sm:block">
              {t('app.subtitle', 'Municipal Operations & Citizen Grievance Redressal')}
            </p>
          </div>
        </Link>
      </div>

      <div className="flex items-center gap-3 sm:gap-4">
        {/* Expandable Multi-Language Switcher (English, Hindi, Telugu) */}
        <nav aria-label="Language selection" className="flex items-center text-xs border border-slate-200 rounded-xl overflow-hidden bg-slate-50 shadow-2xs">
          {supportedLanguages.map((langItem) => {
            const isActive = currentLang === langItem.code;
            return (
              <button
                key={langItem.code}
                type="button"
                onClick={() => changeLanguage(langItem.code)}
                aria-pressed={isActive}
                className={`px-2.5 py-1 text-[11px] font-semibold transition-colors cursor-pointer ${
                  isActive
                    ? 'bg-blue-600 text-white shadow-2xs'
                    : 'text-slate-700 hover:bg-slate-200'
                }`}
              >
                {langItem.nativeLabel}
              </button>
            );
          })}
        </nav>

        {/* User Profile / Auth Actions */}
        {isAuthenticated && user ? (
          <div className="flex items-center gap-3">
            <div className="text-right hidden sm:block">
              <div className="text-xs font-bold text-slate-900">{user.fullName || user.username}</div>
              <div className="text-[10px] text-slate-500 uppercase tracking-wider">
                {user.roles?.[0]?.replace('ROLE_', '') || 'Citizen'}
              </div>
            </div>
            <button
              type="button"
              onClick={logout}
              className="text-xs font-bold text-red-600 hover:text-red-800 px-2.5 py-1.5 border border-red-200 rounded-lg hover:bg-red-50 transition-colors cursor-pointer"
            >
              {t('nav.logout', 'Logout')}
            </button>
          </div>
        ) : (
          <div className="flex items-center gap-2">
            <Link
              to="/login"
              className="text-xs font-semibold text-slate-700 hover:text-blue-600 px-2.5 py-1"
            >
              {t('nav.login', 'Sign In')}
            </Link>
            <Link
              to="/register"
              className="text-xs font-bold bg-blue-600 text-white hover:bg-blue-700 px-3 py-1.5 rounded-lg shadow-xs"
            >
              {t('nav.register', 'Register')}
            </Link>
          </div>
        )}
      </div>
    </header>
  );
};

export default Navbar;
