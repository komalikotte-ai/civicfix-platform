import React from 'react';
import Card from '../../../components/common/Card.jsx';
import Button from '../../../components/common/Button.jsx';

export const MonthlyReportsView = ({ reports = [] }) => {
  const handleExportJson = (report) => {
    const dataStr = 'data:text/json;charset=utf-8,' + encodeURIComponent(JSON.stringify(report, null, 2));
    const downloadAnchor = document.createElement('a');
    downloadAnchor.setAttribute('href', dataStr);
    downloadAnchor.setAttribute('download', `${report.id}-civic-report.json`);
    document.body.appendChild(downloadAnchor);
    downloadAnchor.click();
    downloadAnchor.remove();
  };

  return (
    <div className="flex flex-col gap-4">
      <div className="p-3 bg-purple-50/70 border border-purple-200 rounded-xl text-xs text-purple-900 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <span>📜</span>
          <span>
            <strong>Monthly Governance Accountability Reports:</strong> Public records of municipal grievance handling efficiency.
          </span>
        </div>
        <span className="font-mono text-[10px] text-purple-700 bg-white/70 px-2 py-0.5 rounded border border-purple-100">
          API Integration Point: /api/v1/reports/monthly
        </span>
      </div>

      <div className="space-y-4">
        {reports.map((rep) => (
          <Card key={rep.id} className="p-5 border border-slate-200 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-5">
            <div className="space-y-2 max-w-xl">
              <div className="flex items-center gap-2">
                <h4 className="font-bold text-base text-slate-900">{rep.month}</h4>
                <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-slate-100 text-slate-600">
                  Published: {rep.publishedAt}
                </span>
              </div>
              <p className="text-xs text-slate-600 leading-relaxed">
                {rep.summary}
              </p>

              <div className="flex flex-wrap items-center gap-4 text-xs pt-1">
                <div>
                  <span className="text-slate-400">Filed:</span>{' '}
                  <strong className="text-slate-800">{rep.totalFiled}</strong>
                </div>
                <div>
                  <span className="text-slate-400">Resolved:</span>{' '}
                  <strong className="text-emerald-700 font-bold">{rep.totalResolved}</strong>
                </div>
                <div>
                  <span className="text-slate-400">Avg Resolution:</span>{' '}
                  <strong className="text-slate-800">{rep.avgResolutionTimeHours} hrs</strong>
                </div>
                <div>
                  <span className="text-slate-400">Citizen Rating:</span>{' '}
                  <strong className="text-amber-600 font-bold">⭐ {rep.satisfactionScore} / 5</strong>
                </div>
              </div>
            </div>

            <div className="flex items-center gap-2 self-start md:self-center">
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => handleExportJson(rep)}
                className="text-xs"
              >
                📥 Export JSON
              </Button>
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
};

export default MonthlyReportsView;
