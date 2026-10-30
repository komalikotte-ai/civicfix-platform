import { useState, useCallback, useEffect } from 'react';
import { t, setLanguage as setLang, getLanguage } from '../i18n';

export const useTranslation = () => {
  const [lang, setLangState] = useState(getLanguage);

  useEffect(() => {
    const handleStorageChange = () => {
      setLangState(getLanguage());
    };
    window.addEventListener('storage', handleStorageChange);
    return () => window.removeEventListener('storage', handleStorageChange);
  }, []);

  const changeLanguage = useCallback((newLang) => {
    setLang(newLang);
    setLangState(newLang);
  }, []);

  return {
    t,
    currentLang: lang,
    changeLanguage,
  };
};

export default useTranslation;
