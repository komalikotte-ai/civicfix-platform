import React, { useId } from 'react';

export const Textarea = ({
  label,
  id,
  name,
  value,
  onChange,
  onBlur,
  placeholder,
  error,
  helperText,
  required = false,
  disabled = false,
  rows = 3,
  maxLength,
  className = '',
  ...props
}) => {
  const generatedId = useId();
  const textareaId = id || name || generatedId;
  const errorId = `${textareaId}-error`;
  const helperId = `${textareaId}-helper`;

  const currentLength = value ? String(value).length : 0;

  return (
    <div className="w-full flex flex-col gap-1.5">
      {label && (
        <label
          htmlFor={textareaId}
          className="text-xs font-semibold text-slate-700 select-none flex items-center justify-between"
        >
          <span>
            {label}
            {required && <span className="text-red-500 ml-1" aria-hidden="true">*</span>}
          </span>
          {maxLength && (
            <span className="text-xs text-slate-400 font-normal">
              {currentLength}/{maxLength}
            </span>
          )}
        </label>
      )}

      <textarea
        id={textareaId}
        name={name}
        rows={rows}
        maxLength={maxLength}
        value={value ?? ''}
        onChange={onChange}
        onBlur={onBlur}
        placeholder={placeholder}
        disabled={disabled}
        required={required}
        aria-invalid={Boolean(error)}
        aria-describedby={error ? errorId : helperText ? helperId : undefined}
        className={`w-full px-3 py-2 text-sm rounded-lg border transition-colors focus:outline-none focus:ring-2 focus:ring-offset-1 disabled:bg-slate-50 disabled:text-slate-500 disabled:cursor-not-allowed resize-y ${
          error
            ? 'border-red-300 focus:border-red-500 focus:ring-red-400 bg-red-50/20'
            : 'border-slate-300 focus:border-blue-500 focus:ring-blue-400 bg-white'
        } ${className}`}
        {...props}
      />

      {error ? (
        <p id={errorId} className="text-xs text-red-600 font-medium" role="alert">
          {error}
        </p>
      ) : helperText ? (
        <p id={helperId} className="text-xs text-slate-500">
          {helperText}
        </p>
      ) : null}
    </div>
  );
};

export default Textarea;
