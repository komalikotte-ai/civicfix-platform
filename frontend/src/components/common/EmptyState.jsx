import React from 'react';
import Button from './Button';

export const EmptyState = ({
  title = 'No items found',
  description = 'There are no records matching your current criteria.',
  icon,
  actionLabel,
  onAction,
  className = '',
}) => {
  return (
    <div
      className={`min-h-[220px] w-full flex flex-col items-center justify-center p-8 text-center bg-slate-50/50 rounded-xl border border-dashed border-slate-200 ${className}`}
    >
      <div className="h-12 w-12 rounded-full bg-slate-100 text-slate-400 flex items-center justify-center mb-3">
        {icon || (
          <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"
            />
          </svg>
        )}
      </div>
      <h3 className="text-sm font-bold text-slate-800">{title}</h3>
      {description && <p className="mt-1 text-xs text-slate-500 max-w-sm">{description}</p>}
      {actionLabel && onAction && (
        <Button variant="primary" size="sm" onClick={onAction} className="mt-4">
          {actionLabel}
        </Button>
      )}
    </div>
  );
};

export default EmptyState;
