import React from 'react';
import Spinner from './Spinner';

export const Table = ({
  columns = [],
  data = [],
  keyField = 'id',
  loading = false,
  emptyMessage = 'No records found',
  caption,
  sortBy,
  sortDirection,
  onSort,
  onRowClick,
  className = '',
}) => {
  return (
    <div className={`w-full overflow-x-auto rounded-xl border border-slate-200 bg-white shadow-sm ${className}`}>
      <table className="w-full text-left text-sm text-slate-700 border-collapse">
        {caption && <caption className="sr-only">{caption}</caption>}
        <thead className="bg-slate-50 border-b border-slate-200 text-xs font-semibold text-slate-600 uppercase tracking-wider">
          <tr>
            {columns.map((col) => {
              const isSorted = sortBy && sortBy === col.key;
              return (
                <th
                  key={col.key}
                  scope="col"
                  className={`px-4 py-3 select-none ${col.align === 'right' ? 'text-right' : col.align === 'center' ? 'text-center' : 'text-left'} ${
                    col.sortable ? 'cursor-pointer hover:bg-slate-100/70 transition-colors' : ''
                  } ${col.className || ''}`}
                  onClick={() => col.sortable && onSort && onSort(col.key)}
                  aria-sort={
                    isSorted
                      ? sortDirection === 'asc'
                        ? 'ascending'
                        : 'descending'
                      : undefined
                  }
                >
                  <div
                    className={`inline-flex items-center gap-1.5 ${
                      col.align === 'right' ? 'justify-end' : col.align === 'center' ? 'justify-center' : 'justify-start'
                    }`}
                  >
                    <span>{col.header}</span>
                    {col.sortable && (
                      <span className="text-slate-400 text-xs" aria-hidden="true">
                        {isSorted ? (sortDirection === 'asc' ? '▲' : '▼') : '⇅'}
                      </span>
                    )}
                  </div>
                </th>
              );
            })}
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {loading ? (
            <tr>
              <td colSpan={columns.length} className="px-4 py-12 text-center text-slate-500">
                <div className="flex flex-col items-center justify-center gap-2">
                  <Spinner size="md" />
                  <span className="text-xs font-medium">Loading data...</span>
                </div>
              </td>
            </tr>
          ) : data.length === 0 ? (
            <tr>
              <td colSpan={columns.length} className="px-4 py-12 text-center text-slate-400 text-xs">
                {emptyMessage}
              </td>
            </tr>
          ) : (
            data.map((row, index) => {
              const key = row[keyField] ?? index;
              return (
                <tr
                  key={key}
                  onClick={() => onRowClick && onRowClick(row)}
                  className={`transition-colors hover:bg-slate-50/70 ${
                    onRowClick ? 'cursor-pointer' : ''
                  }`}
                >
                  {columns.map((col) => {
                    const cellValue = row[col.key];
                    return (
                      <td
                        key={col.key}
                        className={`px-4 py-3 text-slate-700 ${
                          col.align === 'right' ? 'text-right' : col.align === 'center' ? 'text-center' : 'text-left'
                        } ${col.cellClassName || ''}`}
                      >
                        {col.render ? col.render(row, index) : cellValue ?? '—'}
                      </td>
                    );
                  })}
                </tr>
              );
            })
          )}
        </tbody>
      </table>
    </div>
  );
};

export default Table;
