import React from 'react';

export const Card = ({ children, title, subtitle, footer, className = '', headerActions }) => {
  return (
    <div className={`bg-white rounded-lg border border-gray-200 shadow-sm overflow-hidden ${className}`}>
      {(title || subtitle || headerActions) && (
        <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
          <div>
            {title && <h3 className="text-base font-semibold text-gray-900">{title}</h3>}
            {subtitle && <p className="text-xs text-gray-500 mt-0.5">{subtitle}</p>}
          </div>
          {headerActions && <div>{headerActions}</div>}
        </div>
      )}
      <div className="px-6 py-4">{children}</div>
      {footer && <div className="px-6 py-3 bg-gray-50 border-t border-gray-100">{footer}</div>}
    </div>
  );
};

export default Card;
