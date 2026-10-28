import React from 'react';

export const Alert = ({ type = 'info', message, className = '', onClose }) => {
  if (!message) return null;

  const styles = {
    info: 'bg-blue-50 text-blue-800 border-blue-200',
    success: 'bg-green-50 text-green-800 border-green-200',
    warning: 'bg-amber-50 text-amber-800 border-amber-200',
    error: 'bg-red-50 text-red-800 border-red-200',
  };

  return (
    <div className={`p-3 rounded-md border text-sm flex items-center justify-between ${styles[type] || styles.info} ${className}`}>
      <span>{message}</span>
      {onClose && (
        <button
          type="button"
          onClick={onClose}
          className="ml-3 font-bold hover:opacity-75 focus:outline-none"
        >
          ×
        </button>
      )}
    </div>
  );
};

export default Alert;
