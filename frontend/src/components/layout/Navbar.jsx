import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { setLanguage, getLanguage, t } from '../../i18n';

export const Navbar = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const [lang, setLangState] = useState(getLanguage());

  const handleLanguageChange = (newLang) => {
    setLanguage(newLang);
    setLangState(newLang);
    window.location.reload();
  };

  return (
    <header className="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-6 sticky top-0 z-10">
      <div className="flex items-center gap-3">
        <Link to="/" className="flex items-center gap-2">
          <div className="h-8 w-8 rounded bg-blue-600 text-white font-bold flex items-center justify-center text-sm">
            CF
          </div>
          <div>
            <h1 className="text-base font-bold text-gray-900 leading-tight">{t('app.title')}</h1>
            <p className="text-[10px] text-gray-500 hidden sm:block">{t('app.subtitle')}</p>
          </div>
        </Link>
      </div>

      <div className="flex items-center gap-4">
        {/* Language Switcher */}
        <div className="flex items-center text-xs border border-gray-200 rounded overflow-hidden">
          <button
            type="button"
            onClick={() => handleLanguageChange('en')}
            className={`px-2 py-1 ${lang === 'en' ? 'bg-blue-600 text-white font-semibold' : 'bg-white text-gray-700'}`}
          >
            EN
          </button>
          <button
            type="button"
            onClick={() => handleLanguageChange('hi')}
            className={`px-2 py-1 ${lang === 'hi' ? 'bg-blue-600 text-white font-semibold' : 'bg-white text-gray-700'}`}
          >
            हिन्दी
          </button>
        </div>

        {/* User profile / Auth actions */}
        {isAuthenticated && user ? (
          <div className="flex items-center gap-3">
            <div className="text-right hidden sm:block">
              <div className="text-xs font-semibold text-gray-800">{user.fullName || user.username}</div>
              <div className="text-[10px] text-gray-500">{user.roles?.[0]?.replace('ROLE_', '')}</div>
            </div>
            <button
              type="button"
              onClick={logout}
              className="text-xs font-medium text-red-600 hover:text-red-800 px-2 py-1 border border-red-200 rounded hover:bg-red-50"
            >
              {t('nav.logout')}
            </button>
          </div>
        ) : (
          <div className="flex items-center gap-2">
            <Link
              to="/login"
              className="text-xs font-medium text-blue-600 hover:text-blue-800 px-3 py-1.5 border border-blue-200 rounded"
            >
              {t('nav.login')}
            </Link>
            <Link
              to="/register"
              className="text-xs font-medium bg-blue-600 text-white hover:bg-blue-700 px-3 py-1.5 rounded"
            >
              {t('nav.register')}
            </Link>
          </div>
        )}
      </div>
    </header>
  );
};

export default Navbar;
