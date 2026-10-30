import React, { useState } from 'react';
import Button from '../../../components/common/Button';
import savedLocationService from '../../../services/savedLocationService';

export const LocationPickerPlaceholder = ({
  latitude,
  longitude,
  addressLine,
  landmark,
  wardId,
  wards = [],
  onLocationChange,
  userId,
}) => {
  const [detectingGps, setDetectingGps] = useState(false);
  const [showSavedModal, setShowSavedModal] = useState(false);
  const savedLocations = savedLocationService.getSavedLocations(userId);

  const handleSimulateGPS = () => {
    setDetectingGps(true);
    // Simulate high precision GPS lookup
    setTimeout(() => {
      // Pick coordinates centered in city municipal zone
      const lat = (17.3850 + (Math.random() - 0.5) * 0.05).toFixed(6);
      const lng = (78.4867 + (Math.random() - 0.5) * 0.05).toFixed(6);

      onLocationChange({
        latitude: parseFloat(lat),
        longitude: parseFloat(lng),
        addressLine: addressLine || 'Near GPS Position Marker, Sector 4',
        landmark: landmark || 'Municipal Smart Pole #402',
      });
      setDetectingGps(false);
    }, 600);
  };

  const handleSelectSavedLocation = (loc) => {
    onLocationChange({
      latitude: loc.latitude,
      longitude: loc.longitude,
      addressLine: loc.addressLine,
      landmark: loc.landmark,
      wardId: loc.wardId,
    });
    setShowSavedModal(false);
  };

  const handleMapPlaceholderClick = (e) => {
    const rect = e.currentTarget.getBoundingClientRect();
    const xRatio = (e.clientX - rect.left) / rect.width;
    const yRatio = (e.clientY - rect.top) / rect.height;

    // Simulate pin placement
    const lat = (17.3850 + (0.5 - yRatio) * 0.04).toFixed(6);
    const lng = (78.4867 + (xRatio - 0.5) * 0.04).toFixed(6);

    onLocationChange({
      latitude: parseFloat(lat),
      longitude: parseFloat(lng),
    });
  };

  return (
    <div className="flex flex-col gap-3">
      {/* Header and Quick Location Buttons */}
      <div className="flex flex-wrap items-center justify-between gap-2">
        <label className="text-xs font-semibold text-slate-700">
          Grievance Location & Geotagging
        </label>
        <div className="flex items-center gap-2">
          {savedLocations.length > 0 && (
            <div className="relative">
              <Button
                type="button"
                size="sm"
                variant="outline"
                onClick={() => setShowSavedModal((prev) => !prev)}
                className="text-xs py-1 px-2.5"
              >
                📍 Use Saved Location ({savedLocations.length})
              </Button>

              {showSavedModal && (
                <div className="absolute right-0 top-full mt-1.5 w-72 bg-white rounded-xl shadow-xl border border-slate-200 z-30 p-2 text-xs">
                  <div className="font-bold text-slate-700 px-2 py-1 border-b border-slate-100 flex items-center justify-between">
                    <span>Select Saved Location</span>
                    <button
                      type="button"
                      onClick={() => setShowSavedModal(false)}
                      className="text-slate-400 hover:text-slate-600 font-bold"
                    >
                      ✕
                    </button>
                  </div>
                  <div className="max-h-48 overflow-y-auto divide-y divide-slate-100 mt-1">
                    {savedLocations.map((loc) => (
                      <button
                        key={loc.id}
                        type="button"
                        onClick={() => handleSelectSavedLocation(loc)}
                        className="w-full text-left p-2 hover:bg-blue-50 rounded flex flex-col gap-0.5 transition-colors"
                      >
                        <span className="font-bold text-blue-700">
                          {loc.label} {loc.isDefault && <span className="text-[10px] bg-blue-100 text-blue-800 px-1 rounded">Default</span>}
                        </span>
                        <span className="text-slate-600 truncate">{loc.addressLine}</span>
                        <span className="text-[10px] text-slate-400">{loc.wardName || `Ward #${loc.wardId}`}</span>
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          <Button
            type="button"
            size="sm"
            variant="ghost"
            onClick={handleSimulateGPS}
            loading={detectingGps}
            className="text-xs py-1 px-2.5 text-blue-600 hover:bg-blue-50"
          >
            🎯 Auto-Detect GPS
          </Button>
        </div>
      </div>

      {/* Map Integration Placeholder Container */}
      <div
        onClick={handleMapPlaceholderClick}
        className="relative h-44 w-full rounded-xl border-2 border-dashed border-blue-300 bg-gradient-to-br from-blue-50/70 via-slate-50 to-indigo-50/70 overflow-hidden cursor-crosshair flex flex-col items-center justify-center text-center p-4 group hover:border-blue-500 transition-colors"
        title="Click anywhere to position pin"
        role="region"
        aria-label="Map location picker placeholder"
      >
        {/* Background Grid Pattern */}
        <div
          className="absolute inset-0 opacity-20 pointer-events-none"
          style={{
            backgroundImage:
              'radial-gradient(circle, #3b82f6 1px, transparent 1px), radial-gradient(circle, #3b82f6 1px, transparent 1px)',
            backgroundSize: '20px 20px',
            backgroundPosition: '0 0, 10px 10px',
          }}
        />

        {/* Pin Marker */}
        <div className="relative z-10 flex flex-col items-center pointer-events-none">
          <div className="w-8 h-8 rounded-full bg-red-600 text-white flex items-center justify-center shadow-lg transform -translate-y-1 animate-bounce">
            📍
          </div>
          <div className="w-3 h-1.5 rounded-full bg-black/20 blur-[1px] -mt-1" />
        </div>

        <div className="relative z-10 mt-2 pointer-events-none">
          <span className="inline-block px-2.5 py-0.5 rounded-full bg-blue-600/10 text-blue-800 text-[11px] font-bold border border-blue-200">
            Phase 2c Map Integration Placeholder
          </span>
          <p className="text-xs text-slate-600 font-medium mt-1">
            Click anywhere inside grid to set coordinates or use GPS button above
          </p>
        </div>

        {/* Readout coordinates bar */}
        <div className="absolute bottom-2 left-2 right-2 flex items-center justify-between px-3 py-1.5 rounded-lg bg-white/90 backdrop-blur-xs border border-slate-200 text-[11px] font-mono text-slate-700 pointer-events-auto">
          <div>
            <span className="text-slate-400 mr-1">LAT:</span>
            <span className="font-semibold text-slate-900">{latitude ? latitude.toFixed(6) : '17.385044'}</span>
            <span className="text-slate-400 mx-2">|</span>
            <span className="text-slate-400 mr-1">LNG:</span>
            <span className="font-semibold text-slate-900">{longitude ? longitude.toFixed(6) : '78.486671'}</span>
          </div>
          <span className="text-[10px] text-emerald-600 font-semibold bg-emerald-50 px-1.5 py-0.5 rounded">
            ✓ Geo-referenced
          </span>
        </div>
      </div>
    </div>
  );
};

export default LocationPickerPlaceholder;
