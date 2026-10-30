import React from 'react';
import Select from '../../../components/common/Select.jsx';
import Button from '../../../components/common/Button.jsx';
import Input from '../../../components/common/Input.jsx';

export const PublicFilters = ({
  filters,
  onChange,
  onReset,
  wards = [],
  departments = [],
  categories = [],
}) => {
  const datePresets = [
    { label: 'All Time', days: null },
    { label: 'Last 7 Days', days: 7 },
    { label: 'Last 30 Days', days: 30 },
    { label: 'Last 90 Days', days: 90 },
  ];

  const handlePresetClick = (days) => {
    if (!days) {
      onChange({ startDate: '', endDate: '', preset: 'all' });
      return;
    }
    const end = new Date();
    const start = new Date();
    start.setDate(end.getDate() - days);

    onChange({
      startDate: start.toISOString().split('T')[0],
      endDate: end.toISOString().split('T')[0],
      preset: `last-${days}`,
    });
  };

  const wardOptions = [
    { value: '', label: 'All Municipal Wards' },
    ...wards.map((w) => ({
      value: String(w.id),
      label: `Ward #${w.wardNumber || w.id} - ${w.name}`,
    })),
  ];

  const deptOptions = [
    { value: '', label: 'All Departments' },
    ...departments.map((d) => ({
      value: String(d.id),
      label: d.name,
    })),
  ];

  const catOptions = [
    { value: '', label: 'All Categories' },
    ...categories.map((c) => ({
      value: c.name || c.value || c,
      label: c.displayName || c.label || c.name || c,
    })),
  ];

  const hasActiveFilters = Boolean(
    filters.wardId || filters.departmentId || filters.category || filters.startDate || filters.endDate
  );

  return (
    <div className="p-4 bg-white rounded-2xl border border-slate-200 shadow-xs flex flex-col gap-4 text-xs">
      {/* Date Range Presets */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-1.5 flex-wrap">
          <span className="font-bold text-slate-700 mr-1">Time Horizon:</span>
          {datePresets.map((p) => {
            const isSelected =
              (!p.days && !filters.startDate) ||
              (p.days && filters.preset === `last-${p.days}`);
            return (
              <button
                key={p.label}
                type="button"
                onClick={() => handlePresetClick(p.days)}
                className={`px-2.5 py-1 rounded-lg font-semibold transition-colors cursor-pointer ${
                  isSelected
                    ? 'bg-blue-600 text-white'
                    : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                }`}
              >
                {p.label}
              </button>
            );
          })}
        </div>

        {hasActiveFilters && (
          <button
            type="button"
            onClick={onReset}
            className="text-blue-600 hover:text-blue-800 font-semibold cursor-pointer underline text-[11px]"
          >
            Reset all filters
          </button>
        )}
      </div>

      {/* Dropdown Filters Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 pt-2 border-t border-slate-100">
        <Select
          label="Jurisdiction / Ward"
          options={wardOptions}
          value={filters.wardId || ''}
          onChange={(e) => onChange({ wardId: e.target.value })}
        />

        <Select
          label="Municipal Department"
          options={deptOptions}
          value={filters.departmentId || ''}
          onChange={(e) => onChange({ departmentId: e.target.value })}
        />

        <Select
          label="Issue Category"
          options={catOptions}
          value={filters.category || ''}
          onChange={(e) => onChange({ category: e.target.value })}
        />
      </div>

      {/* Custom Date Pickers */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2 border-t border-slate-100">
        <Input
          type="date"
          label="Custom Start Date"
          value={filters.startDate || ''}
          onChange={(e) => onChange({ startDate: e.target.value, preset: 'custom' })}
        />

        <Input
          type="date"
          label="Custom End Date"
          value={filters.endDate || ''}
          onChange={(e) => onChange({ endDate: e.target.value, preset: 'custom' })}
        />
      </div>
    </div>
  );
};

export default PublicFilters;
