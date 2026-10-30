import React, { useState, useEffect, useRef } from 'react';
import geocodingService from '../../../services/geocodingService.js';
import { useDebounce } from '../../../hooks/useDebounce.js';

export const AddressSearchBar = ({ onSelectLocation, placeholder = 'Search street, landmark, or ward...' }) => {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [isOpen, setIsOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const containerRef = useRef(null);

  const debouncedQuery = useDebounce(query, 300);

  useEffect(() => {
    if (!debouncedQuery || debouncedQuery.trim().length < 2) {
      setResults([]);
      setIsOpen(false);
      return;
    }

    let active = true;
    setLoading(true);

    geocodingService.searchAddress(debouncedQuery).then((res) => {
      if (active) {
        setResults(res || []);
        setIsOpen((res || []).length > 0);
        setLoading(false);
      }
    });

    return () => {
      active = false;
    };
  }, [debouncedQuery]);

  // Close dropdown on outside click
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleSelect = (item) => {
    setQuery(item.title);
    setIsOpen(false);
    onSelectLocation(item);
  };

  return (
    <div ref={containerRef} className="relative w-full max-w-md">
      <div className="relative flex items-center">
        <span className="absolute left-3 text-slate-400 text-xs pointer-events-none" aria-hidden="true">
          🔍
        </span>
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder={placeholder}
          className="w-full pl-8 pr-8 py-2 text-xs rounded-xl border border-slate-300 bg-white shadow-xs focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
          aria-label="Search address or landmark"
          aria-expanded={isOpen}
        />
        {query && (
          <button
            type="button"
            onClick={() => {
              setQuery('');
              setResults([]);
              setIsOpen(false);
            }}
            className="absolute right-2.5 text-slate-400 hover:text-slate-600 text-xs font-bold p-1 cursor-pointer"
            aria-label="Clear search"
          >
            ✕
          </button>
        )}
      </div>

      {/* Autocomplete Dropdown */}
      {isOpen && (
        <div className="absolute left-0 right-0 top-full mt-1.5 bg-white rounded-xl shadow-xl border border-slate-200 z-50 overflow-hidden divide-y divide-slate-100 max-h-60 overflow-y-auto text-xs animate-in fade-in duration-150">
          {loading && (
            <div className="p-3 text-slate-400 text-center">Searching municipal records...</div>
          )}
          {!loading && results.length === 0 && (
            <div className="p-3 text-slate-500 text-center">No locations found.</div>
          )}
          {!loading &&
            results.map((item) => (
              <button
                key={item.id}
                type="button"
                onClick={() => handleSelect(item)}
                className="w-full text-left p-2.5 hover:bg-blue-50 flex items-start gap-2.5 transition-colors cursor-pointer"
              >
                <span className="text-base mt-0.5" aria-hidden="true">
                  {item.type === 'ward' ? '🏛️' : '📍'}
                </span>
                <div className="flex-1 min-w-0">
                  <div className="font-bold text-slate-900 truncate">{item.title}</div>
                  <div className="text-[11px] text-slate-500 truncate">{item.subtitle}</div>
                </div>
              </button>
            ))}
        </div>
      )}
    </div>
  );
};

export default AddressSearchBar;
