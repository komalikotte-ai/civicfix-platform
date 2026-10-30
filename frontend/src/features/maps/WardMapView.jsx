import React, { useState, useEffect, useCallback } from 'react';
import { mapService } from '../../services/mapService';
import Card from '../../components/common/Card';
import Badge from '../../components/common/Badge';
import Button from '../../components/common/Button';
import Select from '../../components/common/Select';
import LoadingState from '../../components/common/LoadingState';
import ErrorState from '../../components/common/ErrorState';

export const WardMapView = () => {
  const [wards, setWards] = useState([]);
  const [pins, setPins] = useState([]);
  const [selectedWardId, setSelectedWardId] = useState('');
  const [selectedPin, setSelectedPin] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [viewMode, setViewMode] = useState('grid'); // 'grid' | 'accessible-list'

  const loadMapData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [wardsData, pinsData] = await Promise.all([
        mapService.getWards(),
        mapService.getGrievancePins(),
      ]);
      setWards(wardsData);
      setPins(pinsData);
    } catch (err) {
      setError(err.message || 'Failed to load ward map data');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadMapData();
  }, [loadMapData]);

  const filteredPins = selectedWardId
    ? pins.filter((p) => String(p.wardId) === String(selectedWardId))
    : pins;

  const wardOptions = [
    { value: '', label: 'All Wards & Zones' },
    ...wards.map((w) => ({
      value: String(w.id),
      label: `${w.name || `Ward ${w.wardNumber || w.id}`} (Ward #${w.wardNumber || w.id})`,
    })),
  ];

  return (
    <div className="flex flex-col gap-5">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Ward & Grievance Map</h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Geographic overview of reported civic issues across city wards
          </p>
        </div>

        <div className="flex items-center gap-2">
          <div className="flex items-center text-xs border border-slate-300 rounded-lg overflow-hidden bg-white">
            <button
              type="button"
              onClick={() => setViewMode('grid')}
              className={`px-3 py-1.5 font-medium transition-colors ${
                viewMode === 'grid' ? 'bg-blue-600 text-white' : 'text-slate-700 hover:bg-slate-100'
              }`}
            >
              Interactive Map
            </button>
            <button
              type="button"
              onClick={() => setViewMode('accessible-list')}
              className={`px-3 py-1.5 font-medium transition-colors ${
                viewMode === 'accessible-list' ? 'bg-blue-600 text-white' : 'text-slate-700 hover:bg-slate-100'
              }`}
            >
              Accessible List
            </button>
          </div>
          <Button variant="outline" size="sm" onClick={loadMapData}>
            ↻ Refresh
          </Button>
        </div>
      </div>

      {/* Ward Filter */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="w-full sm:w-72">
          <Select
            label="Filter by Ward"
            options={wardOptions}
            value={selectedWardId}
            onChange={(e) => {
              setSelectedWardId(e.target.value);
              setSelectedPin(null);
            }}
          />
        </div>
        <div className="text-xs text-slate-600 font-medium">
          Showing <span className="font-bold text-blue-600">{filteredPins.length}</span> incident pins across{' '}
          <span className="font-bold text-slate-900">{selectedWardId ? '1 selected ward' : `${wards.length} wards`}</span>
        </div>
      </div>

      {loading ? (
        <LoadingState message="Loading GIS boundaries and incident pins..." />
      ) : error ? (
        <ErrorState message={error} onRetry={loadMapData} />
      ) : viewMode === 'grid' ? (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">
          {/* Visual Map Canvas / Spatial Grid Simulator */}
          <div className="lg:col-span-2 bg-slate-900 rounded-2xl p-6 min-h-[440px] relative overflow-hidden flex flex-col justify-between border border-slate-800 shadow-inner">
            {/* Map Header Overlay */}
            <div className="flex items-center justify-between z-10">
              <div className="bg-slate-800/80 backdrop-blur-sm px-3 py-1.5 rounded-lg border border-slate-700 text-slate-300 text-xs font-mono">
                GIS Center: 17.3850° N, 78.4867° E (Civic Grid)
              </div>
              <div className="flex items-center gap-2">
                <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-semibold bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                  <span className="w-1.5 h-1.5 rounded-full bg-emerald-400" /> Live Feed
                </span>
              </div>
            </div>

            {/* Grid Overlay Matrix simulating municipal GIS zone coordinates */}
            <div className="absolute inset-0 opacity-15 pointer-events-none grid grid-cols-6 grid-rows-6 border border-slate-700">
              {Array.from({ length: 36 }).map((_, idx) => (
                <div key={idx} className="border border-slate-700/60" />
              ))}
            </div>

            {/* Map Pins Scattered by Normalized Coordinates */}
            <div className="relative w-full h-[320px] my-4">
              {filteredPins.map((pin) => {
                const isSelected = selectedPin?.id === pin.id;
                // Normalize lat/lng or pin ID into a percentage position
                const leftPos = Math.min(90, Math.max(8, ((pin.longitude - 78.4) * 500) % 85 + 5));
                const topPos = Math.min(85, Math.max(10, ((pin.latitude - 17.3) * 600) % 80 + 8));

                const severityColors = {
                  CRITICAL: 'bg-red-500 text-white ring-red-400',
                  HIGH: 'bg-orange-500 text-white ring-orange-400',
                  MEDIUM: 'bg-amber-500 text-white ring-amber-400',
                  LOW: 'bg-blue-500 text-white ring-blue-400',
                };

                const pinColor = severityColors[pin.severity] || severityColors.MEDIUM;

                return (
                  <button
                    key={pin.id}
                    type="button"
                    onClick={() => setSelectedPin(pin)}
                    style={{ left: `${leftPos}%`, top: `${topPos}%` }}
                    aria-label={`Incident pin: ${pin.title} in ${pin.wardName || 'Ward'}`}
                    className={`absolute transform -translate-x-1/2 -translate-y-1/2 rounded-full p-1.5 transition-all duration-200 focus:outline-none ${
                      isSelected
                        ? 'ring-4 ring-white scale-125 z-20'
                        : 'hover:scale-110 z-10 ring-2 ring-slate-900/50'
                    } ${pinColor}`}
                  >
                    <span className="block w-2.5 h-2.5 rounded-full bg-white shadow-sm" />
                  </button>
                );
              })}
            </div>

            {/* Map Footer Legend */}
            <div className="flex flex-wrap items-center gap-4 bg-slate-800/80 backdrop-blur-sm px-4 py-2 rounded-xl border border-slate-700 text-xs text-slate-300 z-10">
              <span className="font-semibold text-white">Severity Legend:</span>
              <span className="inline-flex items-center gap-1.5">
                <span className="w-2.5 h-2.5 rounded-full bg-red-500" /> Critical
              </span>
              <span className="inline-flex items-center gap-1.5">
                <span className="w-2.5 h-2.5 rounded-full bg-orange-500" /> High
              </span>
              <span className="inline-flex items-center gap-1.5">
                <span className="w-2.5 h-2.5 rounded-full bg-amber-500" /> Medium
              </span>
              <span className="inline-flex items-center gap-1.5">
                <span className="w-2.5 h-2.5 rounded-full bg-blue-500" /> Low
              </span>
            </div>
          </div>

          {/* Selected Pin Details Panel */}
          <div className="flex flex-col gap-3">
            <Card className="p-5 flex-1 flex flex-col justify-between">
              {selectedPin ? (
                <div className="flex flex-col gap-4">
                  <div className="flex items-start justify-between gap-2">
                    <div>
                      <span className="font-mono text-xs font-bold text-blue-600">
                        {selectedPin.trackingNumber || `CF-${selectedPin.id}`}
                      </span>
                      <h3 className="text-base font-bold text-slate-900 mt-1">
                        {selectedPin.title}
                      </h3>
                    </div>
                    <Badge status={selectedPin.status}>{selectedPin.status}</Badge>
                  </div>

                  <div className="flex flex-col gap-2 text-xs text-slate-600 bg-slate-50 p-3 rounded-lg border border-slate-100">
                    <div className="flex justify-between">
                      <span className="font-medium text-slate-500">Category:</span>
                      <span className="font-semibold text-slate-800">{selectedPin.category || 'General'}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="font-medium text-slate-500">Ward:</span>
                      <span className="font-semibold text-slate-800">{selectedPin.wardName || `Ward #${selectedPin.wardId}`}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="font-medium text-slate-500">Severity:</span>
                      <span className="font-semibold text-slate-800">{selectedPin.severity || 'MEDIUM'}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="font-medium text-slate-500">Coordinates:</span>
                      <span className="font-mono text-[11px] text-slate-700">
                        {selectedPin.latitude?.toFixed(4)}, {selectedPin.longitude?.toFixed(4)}
                      </span>
                    </div>
                  </div>

                  {selectedPin.addressLine && (
                    <div className="text-xs text-slate-600">
                      <span className="font-semibold text-slate-700">Address / Landmark:</span>
                      <p className="mt-0.5 text-slate-500">{selectedPin.addressLine}</p>
                    </div>
                  )}

                  <div className="pt-3 border-t border-slate-100 flex items-center gap-2">
                    <a
                      href={`/complaints/${selectedPin.id}`}
                      className="w-full inline-flex items-center justify-center px-4 py-2 bg-blue-600 text-white rounded-lg text-xs font-semibold hover:bg-blue-700 transition-colors"
                    >
                      Open Full Incident Record →
                    </a>
                  </div>
                </div>
              ) : (
                <div className="flex flex-col items-center justify-center text-center p-6 text-slate-400 h-full">
                  <div className="text-3xl mb-2">📍</div>
                  <h4 className="text-sm font-semibold text-slate-700">Select an Incident Pin</h4>
                  <p className="text-xs text-slate-500 mt-1 max-w-xs">
                    Click any marker on the map to view grievance details, coordinates, and resolution progress.
                  </p>
                </div>
              )}
            </Card>
          </div>
        </div>
      ) : (
        /* Accessible Table Alternative */
        <Card className="p-4">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-slate-700 border-collapse">
              <thead className="bg-slate-50 border-b border-slate-200 text-xs font-semibold text-slate-600">
                <tr>
                  <th scope="col" className="px-4 py-3">Tracking ID</th>
                  <th scope="col" className="px-4 py-3">Title</th>
                  <th scope="col" className="px-4 py-3">Ward</th>
                  <th scope="col" className="px-4 py-3">Coordinates</th>
                  <th scope="col" className="px-4 py-3">Severity</th>
                  <th scope="col" className="px-4 py-3">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filteredPins.map((pin) => (
                  <tr key={pin.id} className="hover:bg-slate-50">
                    <td className="px-4 py-2.5 font-mono text-xs font-bold text-blue-600">
                      <a href={`/complaints/${pin.id}`} className="hover:underline">
                        {pin.trackingNumber || `CF-${pin.id}`}
                      </a>
                    </td>
                    <td className="px-4 py-2.5 font-medium text-slate-900">{pin.title}</td>
                    <td className="px-4 py-2.5 text-xs text-slate-600">{pin.wardName || `Ward #${pin.wardId}`}</td>
                    <td className="px-4 py-2.5 font-mono text-xs text-slate-500">
                      {pin.latitude?.toFixed(4)}, {pin.longitude?.toFixed(4)}
                    </td>
                    <td className="px-4 py-2.5 text-xs font-semibold">{pin.severity || 'MEDIUM'}</td>
                    <td className="px-4 py-2.5">
                      <Badge status={pin.status}>{pin.status}</Badge>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      )}
    </div>
  );
};

export default WardMapView;
