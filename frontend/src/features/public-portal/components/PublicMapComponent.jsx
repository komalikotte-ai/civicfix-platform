import React, { useState } from 'react';
import InteractiveMap from '../../maps/components/InteractiveMap.jsx';
import Badge from '../../../components/common/Badge.jsx';
import Button from '../../../components/common/Button.jsx';

export const PublicMapComponent = ({ pins = [], wards = [] }) => {
  const [selectedPin, setSelectedPin] = useState(null);
  const [showHeatmap, setShowHeatmap] = useState(false);
  const [categoryFilter, setCategoryFilter] = useState('');

  const filteredPins = categoryFilter
    ? pins.filter((p) => p.category?.toUpperCase() === categoryFilter.toUpperCase())
    : pins;

  const categories = Array.from(new Set(pins.map((p) => p.category).filter(Boolean)));

  return (
    <div className="flex flex-col gap-3">
      {/* Privacy Notice Banner */}
      <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-xl text-xs text-emerald-800 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <span>🔒</span>
          <span>
            <strong>Citizen Privacy Guaranteed:</strong> Individual complainant identities and contact numbers are strictly scrubbed in public transparency views.
          </span>
        </div>
      </div>

      {/* Controls Bar */}
      <div className="flex flex-wrap items-center justify-between gap-3 p-3 bg-white rounded-xl border border-slate-200 text-xs">
        <div className="flex items-center gap-2">
          <span className="font-semibold text-slate-700">Category:</span>
          <select
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
            className="px-2.5 py-1 rounded-lg border border-slate-300 text-xs bg-white text-slate-800 focus:outline-none"
          >
            <option value="">All Categories ({pins.length})</option>
            {categories.map((c) => (
              <option key={c} value={c}>
                {c}
              </option>
            ))}
          </select>
        </div>

        <Button
          variant={showHeatmap ? 'primary' : 'outline'}
          size="sm"
          onClick={() => setShowHeatmap((prev) => !prev)}
          className="text-xs"
        >
          {showHeatmap ? '🔥 Heatmap: Active' : '🔥 Show Density Heatmap'}
        </Button>
      </div>

      {/* Map */}
      <div className="relative">
        <InteractiveMap
          center={[17.385044, 78.486671]}
          zoom={13}
          markers={filteredPins}
          selectedMarkerId={selectedPin?.id}
          wards={wards}
          showHeatmap={showHeatmap}
          showBoundaries={true}
          onMarkerClick={(m) => setSelectedPin(m.data || m)}
          className="h-[460px] w-full"
        />

        {/* Selected Complaint Floating Card */}
        {selectedPin && (
          <div
            role="dialog"
            aria-label="Public grievance details"
            className="absolute bottom-4 left-4 right-4 sm:left-auto sm:right-4 sm:w-80 bg-white/95 backdrop-blur-md rounded-2xl shadow-xl border border-slate-200 p-4 z-40 text-xs flex flex-col gap-2.5 animate-in slide-in-from-bottom duration-150"
          >
            <div className="flex items-start justify-between border-b border-slate-100 pb-2">
              <div>
                <span className="font-mono font-bold text-blue-600 bg-blue-50 px-2 py-0.5 rounded text-[11px]">
                  {selectedPin.trackingNumber}
                </span>
                <h4 className="font-bold text-slate-900 mt-1 line-clamp-1">{selectedPin.title}</h4>
              </div>
              <button
                type="button"
                onClick={() => setSelectedPin(null)}
                className="text-slate-400 hover:text-slate-600 font-bold p-1 cursor-pointer"
              >
                ✕
              </button>
            </div>

            <div className="flex items-center gap-1.5">
              <Badge status={selectedPin.status}>{selectedPin.status}</Badge>
              <span className="px-1.5 py-0.5 rounded text-[10px] bg-slate-100 text-slate-600 font-medium">
                {selectedPin.category || 'General'}
              </span>
            </div>

            <div className="text-[11px] text-slate-600 space-y-0.5 bg-slate-50 p-2 rounded-lg border border-slate-100">
              <div>
                <span className="text-slate-400">Ward:</span>{' '}
                <strong className="text-slate-800">{selectedPin.wardName}</strong>
              </div>
              <div>
                <span className="text-slate-400">Reported:</span>{' '}
                <strong className="text-slate-800">
                  {selectedPin.createdAt ? new Date(selectedPin.createdAt).toLocaleDateString() : 'N/A'}
                </strong>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default PublicMapComponent;
