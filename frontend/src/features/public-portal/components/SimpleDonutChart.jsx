import React from 'react';

export const SimpleDonutChart = ({
  data = [],
  title = 'Status Breakdown',
  size = 180,
  strokeWidth = 24,
}) => {
  const total = data.reduce((acc, curr) => acc + (curr.value || 0), 0);

  if (total === 0) {
    return (
      <div className="py-8 text-center text-xs text-slate-400 bg-slate-50 rounded-xl border border-dashed border-slate-200">
        No breakdown data to display.
      </div>
    );
  }

  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  let accumulatedOffset = 0;

  return (
    <div className="flex flex-col gap-3">
      {title && (
        <h4 className="text-xs font-bold text-slate-700 uppercase tracking-wider">
          {title}
        </h4>
      )}

      <div className="flex flex-col sm:flex-row items-center justify-center gap-6">
        {/* SVG Donut */}
        <div className="relative flex items-center justify-center" style={{ width: size, height: size }}>
          <svg width={size} height={size} className="transform -rotate-90">
            {data.map((item, idx) => {
              const val = item.value || 0;
              const ratio = val / total;
              const dashLength = ratio * circumference;
              const offset = accumulatedOffset;
              accumulatedOffset += dashLength;

              return (
                <circle
                  key={idx}
                  cx={size / 2}
                  cy={size / 2}
                  r={radius}
                  fill="transparent"
                  stroke={item.color || '#3b82f6'}
                  strokeWidth={strokeWidth}
                  strokeDasharray={`${dashLength} ${circumference}`}
                  strokeDashoffset={-offset}
                  className="transition-all duration-500"
                />
              );
            })}
          </svg>

          {/* Center Summary */}
          <div className="absolute inset-0 flex flex-col items-center justify-center text-center pointer-events-none">
            <span className="text-2xl font-black text-slate-900 leading-none">{total}</span>
            <span className="text-[10px] text-slate-500 font-semibold uppercase mt-0.5">Total</span>
          </div>
        </div>

        {/* Legend */}
        <div className="flex flex-col gap-2 text-xs">
          {data.map((item, idx) => {
            const val = item.value || 0;
            const pct = Math.round((val / total) * 100);

            return (
              <div key={idx} className="flex items-center gap-2.5">
                <span
                  className="w-3 h-3 rounded-full flex-shrink-0"
                  style={{ backgroundColor: item.color || '#3b82f6' }}
                  aria-hidden="true"
                />
                <span className="text-slate-700 font-medium">{item.label}:</span>
                <span className="font-bold text-slate-900">{val}</span>
                <span className="text-slate-400 text-[11px]">({pct}%)</span>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};

export default SimpleDonutChart;
