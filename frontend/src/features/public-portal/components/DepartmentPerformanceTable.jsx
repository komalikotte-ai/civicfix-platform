import React from 'react';
import Table from '../../../components/common/Table.jsx';

export const DepartmentPerformanceTable = ({ departmentPerformance = [] }) => {
  const columns = [
    {
      key: 'name',
      header: 'Department',
      render: (row) => (
        <div>
          <div className="font-bold text-slate-900">{row.name}</div>
          <span className="font-mono text-[10px] text-slate-500 uppercase">{row.code}</span>
        </div>
      ),
    },
    {
      key: 'totalTickets',
      header: 'Total Assigned',
      align: 'right',
      render: (row) => <span className="font-semibold text-slate-800">{row.totalTickets}</span>,
    },
    {
      key: 'resolvedTickets',
      header: 'Resolved',
      align: 'right',
      render: (row) => <span className="font-bold text-emerald-600">{row.resolvedTickets}</span>,
    },
    {
      key: 'resolutionRate',
      header: 'Resolution Efficiency',
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
      key: 'avgSlaHours',
      header: 'Avg Turnaround',
      align: 'right',
      render: (row) => <span className="font-mono text-xs text-slate-700">{row.avgSlaHours} hrs</span>,
    },
    {
      key: 'slaCompliancePercent',
      header: 'SLA Compliance',
      align: 'right',
      render: (row) => (
        <span className="text-xs font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-100">
          {row.slaCompliancePercent}%
        </span>
      ),
    },
  ];

  return <Table columns={columns} data={departmentPerformance} />;
};

export default DepartmentPerformanceTable;
