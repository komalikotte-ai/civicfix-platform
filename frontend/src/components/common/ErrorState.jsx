import React from 'react';
import Button from './Button';

export const ErrorState = ({
  title = 'Something went wrong',
  message = 'We encountered an error while fetching or processing this data. Please try again.',
  errorCode,
  onRetry,
  className = '',
}) => {
  return (
    <div
      role="alert"
      className={`min-h-[220px] w-full flex flex-col items-center justify-center p-8 text-center bg-red-50/40 rounded-xl border border-red-200/80 ${className}`}
    >
      <div className="h-12 w-12 rounded-full bg-red-100 text-red-600 flex items-center justify-center mb-3">
        <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="2"
            d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
          />
        </svg>
      </div>
      <h3 className="text-base font-bold text-slate-900">{title}</h3>
      <p className="mt-1 text-xs text-slate-600 max-w-md">{message}</p>
      {errorCode && (
        <span className="mt-2 inline-block px-2 py-0.5 text-[11px] font-mono text-red-700 bg-red-100 rounded">
          Error code: {errorCode}
        </span>
      )}
      {onRetry && (
        <Button variant="outline" size="sm" onClick={onRetry} className="mt-4">
          Try Again
        </Button>
      )}
    </div>
  );
};

export default ErrorState;
