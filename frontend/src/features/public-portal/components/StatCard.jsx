import React from 'react';
import Card from '../../../components/common/Card.jsx';

export const StatCard = ({
  title,
  value,
  subtitle,
  icon = '📊',
  trend,
  trendPositive = true,
  variant = 'blue', // 'blue' | 'emerald' | 'amber' | 'purple'
}) => {
  const borderColors = {
    blue: 'border-l-blue-500',
    emerald: 'border-l-emerald-500',
    amber: 'border-l-amber-500',
    purple: 'border-l-purple-500',
  };

  const iconBgs = {
    blue: 'bg-blue-50 text-blue-600',
    emerald: 'bg-emerald-50 text-emerald-600',
    amber: 'bg-amber-50 text-amber-600',
    purple: 'bg-purple-50 text-purple-600',
  };

  return (
    <Card className={`p-5 flex items-center justify-between border-l-4 ${borderColors[variant] || 'border-l-blue-500'} shadow-xs`}>
      <div className="flex-1 min-w-0 pr-3">
        <div className="text-xs font-semibold text-slate-500 uppercase tracking-wider truncate">
          {title}
        </div>
        <div className="text-2xl sm:text-3xl font-black text-slate-900 mt-1">
          {value}
        </div>
        {subtitle && (
          <div className="text-[11px] text-slate-500 mt-0.5 truncate">
            {subtitle}
          </div>
        )}
        {trend && (
          <div className={`text-[11px] font-bold mt-1.5 flex items-center gap-1 ${trendPositive ? 'text-emerald-600' : 'text-rose-600'}`}>
            <span>{trendPositive ? '↑' : '↓'}</span>
            <span>{trend}</span>
          </div>
        )}
      </div>

      <div
        className={`w-12 h-12 rounded-xl flex items-center justify-center text-xl flex-shrink-0 font-bold ${iconBgs[variant] || 'bg-blue-50 text-blue-600'}`}
        aria-hidden="true"
      >
        {icon}
      </div>
    </Card>
  );
};

export default StatCard;
