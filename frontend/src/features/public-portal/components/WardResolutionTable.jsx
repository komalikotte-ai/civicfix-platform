import React, { useState } from 'react';
import Table from '../../../components/common/Table.jsx';
import Pagination from '../../../components/common/Pagination.jsx';

export const WardResolutionTable = ({ wardPerformance = [] }) => {
  const [page, setPage] = useState(0);
  const pageSize = 10;

  const totalPages = Math.ceil(wardPerformance.length / pageSize) || 1;
  const paginated = wardPerformance.slice(page * pageSize, (page + 1) * pageSize);

  const columns = [
    {
      key: 'wardNumber',
      header: 'Ward ID',
      render: (row) => (
        <span className="font-mono text-xs font-bold text-blue-700 bg-blue-50 px-2 py-0.5 rounded">
          #{row.wardNumber}
        </span>
      ),
    },
    {
      key: 'wardName',
      header: 'Ward Name & Zone',
      render: (row) => (
        <div>
          <div className="font-bold text-slate-900">{row.wardName}</div>
          <div className="text-[11px] text-slate-500">{row.zoneName}</div>
        </div>
      ),
    },
    {
      key: 'totalComplaints',
      header: 'Complaints',
      align: 'right',
      render: (row) => <span className="font-semibold text-slate-800">{row.totalComplaints}</span>,
    },
    {
      key: 'resolvedComplaints',
      header: 'Resolved',
      align: 'right',
      render: (row) => <span className="font-bold text-emerald-600">{row.resolvedComplaints}</span>,
    },
    {
      key: 'activeComplaints',
      header: 'Active',
      align: 'right',
      render: (row) => <span className="font-bold text-amber-600">{row.activeComplaints}</span>,
    },
    {
      key: 'resolutionRate',
      header: 'Resolution Rate',
      render: (row) => (
        <div className="flex items-center gap-2 min-w-[120px]">
          <div className="flex-1 h-2 rounded-full bg-slate-100 overflow-hidden">
            <div
              className={`h-full rounded-full ${
                row.resolutionRate >= 80
                  ? 'bg-emerald-500'
                  : row.resolutionRate >= 50
                  ? 'bg-amber-500'
                  : 'bg-rose-500'
              }`}
              style={{ width: `${row.resolutionRate}%` }}
            />
          </div>
          <span className="font-bold text-xs text-slate-900 w-10 text-right">
            {row.resolutionRate}%
          </span>
        </div>
      ),
    },
    {
      key: 'slaOnTimePercent',
      header: 'SLA Adherence',
      align: 'right',
      render: (row) => (
        <span className="text-xs font-semibold text-slate-700 bg-slate-100 px-2 py-0.5 rounded">
          {row.slaOnTimePercent}%
        </span>
      ),
    },
  ];

  return (
    <div className="flex flex-col gap-3">
      <Table columns={columns} data={paginated} />
      {totalPages > 1 && (
        <Pagination currentPage={page} totalPages={totalPages} onPageChange={(p) => setPage(p)} />
      )}
    </div>
  );
};

export default WardResolutionTable;
