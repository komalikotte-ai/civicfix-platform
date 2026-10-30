import en from './en.js';
import hi from './hi.js';

const dictionaries = { en, hi };
let currentLang = (typeof localStorage !== 'undefined' && localStorage.getItem('civicfix_lang')) || 'en';

export const setLanguage = (lang) => {
  if (dictionaries[lang]) {
    currentLang = lang;
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem('civicfix_lang', lang);
    }
  }
};

export const getLanguage = () => currentLang;

export const t = (keyPath, fallback = '') => {
  const keys = keyPath.split('.');
  let current = dictionaries[currentLang] || dictionaries.en;

  for (const k of keys) {
    if (current && typeof current === 'object' && k in current) {
      current = current[k];
    } else {
      return fallback || keyPath;
    }
  }

  return current || fallback || keyPath;
};

export default { t, setLanguage, getLanguage };
