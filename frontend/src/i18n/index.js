import en from './en.js';
import hi from './hi.js';
import te from './te.js';

// Expandable Language Registry
const languageRegistry = new Map([
  ['en', { dict: en, label: 'English', nativeLabel: 'English', dir: 'ltr' }],
  ['hi', { dict: hi, label: 'Hindi', nativeLabel: 'हिन्दी', dir: 'ltr' }],
  ['te', { dict: te, label: 'Telugu', nativeLabel: 'తెలుగు', dir: 'ltr' }],
]);

const listeners = new Set();

let currentLang =
  (typeof localStorage !== 'undefined' && localStorage.getItem('civicfix_lang')) || 'en';

if (!languageRegistry.has(currentLang)) {
  currentLang = 'en';
}

/**
 * Registers an expandable new language at runtime.
 */
export const registerLanguage = (code, dict, metadata = {}) => {
  const normalized = code.toLowerCase();
  languageRegistry.set(normalized, {
    dict,
    label: metadata.label || code.toUpperCase(),
    nativeLabel: metadata.nativeLabel || metadata.label || code.toUpperCase(),
    dir: metadata.dir || 'ltr',
  });
};

/**
 * Returns list of currently registered languages with native labels.
 */
export const getSupportedLanguages = () => {
  return Array.from(languageRegistry.entries()).map(([code, meta]) => ({
    code,
    label: meta.label,
    nativeLabel: meta.nativeLabel,
    dir: meta.dir,
  }));
};

/**
 * Switches the active language, saves to localStorage, and notifies listeners.
 */
export const setLanguage = (lang) => {
  const target = lang ? lang.toLowerCase() : 'en';
  if (languageRegistry.has(target)) {
    currentLang = target;
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem('civicfix_lang', target);
    }
    // Update HTML lang attribute for screen-readers
    if (typeof document !== 'undefined' && document.documentElement) {
      document.documentElement.lang = target;
    }
    listeners.forEach((fn) => fn(currentLang));
  }
};

export const getLanguage = () => currentLang;

/**
 * Subscribes to language changes. Returns unsubscribe function.
 */
export const onLanguageChange = (listener) => {
  listeners.add(listener);
  return () => listeners.delete(listener);
};

/**
 * Translates a key path with optional parameter interpolation.
 * e.g. t('common.welcome', { name: 'Komal' })
 */
export const t = (keyPath, paramsOrFallback = {}) => {
  if (!keyPath || typeof keyPath !== 'string') return '';

  const fallback = typeof paramsOrFallback === 'string' ? paramsOrFallback : '';
  const params = typeof paramsOrFallback === 'object' && paramsOrFallback !== null ? paramsOrFallback : {};

  const keys = keyPath.split('.');

  const resolveInDict = (dict) => {
    let current = dict;
    for (const k of keys) {
      if (current && typeof current === 'object' && k in current) {
        current = current[k];
      } else {
        return null;
      }
    }
    return typeof current === 'string' ? current : null;
  };

  const activeDict = languageRegistry.get(currentLang)?.dict;
  const fallbackDict = languageRegistry.get('en')?.dict;

  let text = resolveInDict(activeDict) || resolveInDict(fallbackDict) || fallback || keyPath;

  // Parameter interpolation: replaces {param} or {{param}}
  if (params && typeof params === 'object') {
    Object.entries(params).forEach(([paramKey, val]) => {
      text = text.replace(new RegExp(`\\{\\{?${paramKey}\\}?\\}`, 'g'), String(val));
    });
  }

  return text;
};

export default {
  t,
  setLanguage,
  getLanguage,
  registerLanguage,
  getSupportedLanguages,
  onLanguageChange,
};
