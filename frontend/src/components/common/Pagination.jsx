import React from 'react';
import Button from './Button';

export const Pagination = ({
  page = 0,
  totalPages = 1,
  totalElements = 0,
  size = 20,
  onPageChange,
  className = '',
}) => {
  if (totalPages <= 1) return null;

  const currentDisplayPage = page + 1;
  const startItem = page * size + 1;
  const endItem = Math.min((page + 1) * size, totalElements);

  return (
    <div className={`flex items-center justify-between px-4 py-3 bg-white border-t border-gray-200 sm:px-6 ${className}`}>
      <div className="text-xs text-gray-700">
        Showing <span className="font-semibold">{startItem}</span> to{' '}
        <span className="font-semibold">{endItem}</span> of{' '}
        <span className="font-semibold">{totalElements}</span> results
      </div>

      <div className="flex items-center gap-2">
        <Button
          variant="outline"
          size="sm"
          disabled={page <= 0}
          onClick={() => onPageChange(page - 1)}
        >
          Previous
        </Button>
        <span className="text-xs font-medium text-gray-600">
          Page {currentDisplayPage} of {totalPages}
        </span>
        <Button
          variant="outline"
          size="sm"
          disabled={page >= totalPages - 1}
          onClick={() => onPageChange(page + 1)}
        >
          Next
        </Button>
      </div>
    </div>
  );
};

export default Pagination;
