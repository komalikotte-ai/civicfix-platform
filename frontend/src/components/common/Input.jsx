import React from 'react';

export const Input = ({
  label,
  id,
  name,
  type = 'text',
  value,
  onChange,
  placeholder = '',
  error = '',
  helperText = '',
  required = false,
  disabled = false,
  className = '',
  ...props
}) => {
  const inputId = id || name;

  return (
    <div className={`flex flex-col gap-1 w-full ${className}`}>
      {label && (
        <label htmlFor={inputId} className="text-xs font-semibold text-gray-700">
          {label} {required && <span className="text-red-500">*</span>}
        </label>
      )}
      <input
        id={inputId}
        name={name}
        type={type}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        disabled={disabled}
        className={`px-3 py-2 text-sm rounded border ${
          error ? 'border-red-500 focus:ring-red-400' : 'border-gray-300 focus:ring-blue-500'
        } focus:outline-none focus:ring-2 disabled:bg-gray-100 disabled:text-gray-400`}
        {...props}
      />
      {error && <span className="text-xs text-red-600 font-medium">{error}</span>}
      {!error && helperText && <span className="text-xs text-gray-500">{helperText}</span>}
    </div>
  );
};

export default Input;
