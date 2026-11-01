import { useState, useCallback, useEffect } from 'react';
import { t, setLanguage as setLang, getLanguage, getSupportedLanguages, onLanguageChange } from '../i18n/index.js';

export const useTranslation = () => {
  const [lang, setLangState] = useState(getLanguage);

  useEffect(() => {
    // Subscribe to internal language changes
    const unsubscribe = onLanguageChange((newLang) => {
      setLangState(newLang);
    });

    const handleStorageChange = (e) => {
      if (e.key === 'civicfix_lang' && e.newValue) {
        setLangState(e.newValue);
      }
    };
    window.addEventListener('storage', handleStorageChange);

    return () => {
      unsubscribe();
      window.removeEventListener('storage', handleStorageChange);
    };
  }, []);

  const changeLanguage = useCallback((newLang) => {
    setLang(newLang);
    setLangState(newLang);
  }, []);

  return {
    t,
    currentLang: lang,
    changeLanguage,
    supportedLanguages: getSupportedLanguages(),
  };
};

export default useTranslation;
