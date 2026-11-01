import React from 'react';
import Spinner from './Spinner';

export const LoadingState = ({
  message = 'Loading data, please wait...',
  size = 'lg',
  className = '',
  inline = false,
}) => {
  if (inline) {
    return (
      <div
        className={`inline-flex items-center gap-2 text-xs text-slate-500 ${className}`}
        role="status"
        aria-live="polite"
      >
        <Spinner size="sm" />
        <span>{message}</span>
      </div>
    );
  }

  return (
    <div
      className={`min-h-[220px] w-full flex flex-col items-center justify-center p-8 text-center ${className}`}
      role="status"
      aria-live="polite"
    >
      <Spinner size={size} />
      {message && <p className="mt-3 text-sm text-slate-600 font-medium">{message}</p>}
    </div>
  );
};

export default LoadingState;
