import { useState, useCallback } from 'react';

export const usePagination = (initialPage = 0, initialSize = 20, initialSortBy = 'createdAt', initialSortDir = 'desc') => {
  const [page, setPage] = useState(initialPage);
  const [size, setSize] = useState(initialSize);
  const [sortBy, setSortBy] = useState(initialSortBy);
  const [sortDirection, setSortDirection] = useState(initialSortDir);

  const nextPage = useCallback(() => setPage((prev) => prev + 1), []);
  const prevPage = useCallback(() => setPage((prev) => Math.max(0, prev - 1)), []);
  const resetPage = useCallback(() => setPage(0), []);

  const getQueryParams = useCallback(() => ({
    page,
    size,
    sortBy,
    sortDirection,
  }), [page, size, sortBy, sortDirection]);

  return {
    page,
    setPage,
    size,
    setSize,
    sortBy,
    setSortBy,
    sortDirection,
    setSortDirection,
    nextPage,
    prevPage,
    resetPage,
    getQueryParams,
  };
};

export default usePagination;
