import React from 'react';
import Card from '../../../components/common/Card.jsx';
import Badge from '../../../components/common/Badge.jsx';

export const PublicProjectTracker = ({ projects = [] }) => {
  return (
    <div className="flex flex-col gap-4">
      {/* Transparency Note */}
      <div className="p-3 bg-blue-50/70 border border-blue-200 rounded-xl text-xs text-blue-900 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <span>🏗️</span>
          <span>
            <strong>Public Infrastructure Accountability:</strong> Projects funded by municipal corporation budget with milestone audits.
          </span>
        </div>
        <span className="font-mono text-[10px] text-blue-700 bg-white/70 px-2 py-0.5 rounded border border-blue-100">
          API Integration Point: /api/v1/public/projects
        </span>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {projects.map((proj) => (
          <Card key={proj.id} className="p-5 flex flex-col justify-between gap-4 border border-slate-200 shadow-xs hover:shadow-md transition-shadow">
            <div className="space-y-2">
              <div className="flex items-center justify-between gap-2">
                <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-slate-100 text-slate-700">
                  {proj.wardName}
                </span>
                <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                  proj.status === 'NEAR_COMPLETION'
                    ? 'bg-emerald-100 text-emerald-800'
                    : 'bg-blue-100 text-blue-800'
                }`}>
                  {proj.status.replace('_', ' ')}
                </span>
              </div>

              <h4 className="font-bold text-sm text-slate-900 leading-snug">
                {proj.name}
              </h4>

              <p className="text-xs text-slate-500 line-clamp-2">
                {proj.description}
              </p>
            </div>

            <div className="space-y-3 pt-3 border-t border-slate-100 text-xs">
              {/* Progress Bar */}
              <div>
                <div className="flex items-center justify-between text-[11px] mb-1">
                  <span className="text-slate-500 font-medium">Milestone Progress</span>
                  <span className="font-bold text-slate-900">{proj.progressPercent}%</span>
                </div>
                <div className="w-full h-2.5 rounded-full bg-slate-100 overflow-hidden">
                  <div
                    className="h-full rounded-full bg-blue-600 transition-all duration-500"
                    style={{ width: `${proj.progressPercent}%` }}
                  />
                </div>
              </div>

              <div className="flex items-center justify-between text-[11px] text-slate-500">
                <span>Budget: <strong className="text-slate-900">₹{proj.budgetLakhs} L</strong></span>
                <span>Target: <strong className="text-slate-900">{proj.targetCompletionDate}</strong></span>
              </div>
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
};

export default PublicProjectTracker;
