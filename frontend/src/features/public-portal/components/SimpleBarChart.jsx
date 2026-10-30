import React from 'react';

export const SimpleBarChart = ({
  data = [],
  title = 'Performance Comparison',
  height = 240,
  valueKey = 'value',
  labelKey = 'label',
  color = '#3b82f6',
}) => {
  if (!data || data.length === 0) {
    return (
      <div className="py-8 text-center text-xs text-slate-400 bg-slate-50 rounded-xl border border-dashed border-slate-200">
        No comparative data available.
      </div>
    );
  }

  const maxValue = Math.max(...data.map((d) => d[valueKey] || 0), 10);

  return (
    <div className="flex flex-col gap-3">
      {title && (
        <h4 className="text-xs font-bold text-slate-700 uppercase tracking-wider">
          {title}
        </h4>
      )}

      <div
        className="space-y-3"
        role="img"
        aria-label={`Bar chart displaying ${title}`}
      >
        {data.map((item, idx) => {
          const val = item[valueKey] || 0;
          const pct = Math.min(100, Math.round((val / maxValue) * 100));

          return (
            <div key={idx} className="flex flex-col gap-1 text-xs">
              <div className="flex items-center justify-between text-slate-700">
                <span className="font-semibold truncate max-w-xs">{item[labelKey]}</span>
                <span className="font-mono font-bold text-slate-900">{val}%</span>
              </div>

              <div className="w-full h-3 rounded-full bg-slate-100 overflow-hidden flex">
                <div
                  className="h-full rounded-full transition-all duration-500"
                  style={{
                    width: `${pct}%`,
                    backgroundColor: item.color || color,
                  }}
                />
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default SimpleBarChart;
