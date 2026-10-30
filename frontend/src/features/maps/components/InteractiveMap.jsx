import React, { useRef, useEffect } from 'react';
import useMapInstance from '../hooks/useMapInstance.js';
import Button from '../../../components/common/Button.jsx';

export const InteractiveMap = ({
  center = [17.385044, 78.486671],
  zoom = 13,
  markers = [],
  selectedMarkerId,
  wards = [],
  showHeatmap = false,
  showBoundaries = true,
  provider = 'vector-canvas',
  onMarkerClick,
  onMapClick,
  onBoundaryClick,
  className = 'h-96 w-full',
}) => {
  const containerRef = useRef(null);

  const { map, isReady } = useMapInstance(containerRef, {
    provider,
    center,
    zoom,
    onClick: onMapClick,
    onMarkerClick,
    onBoundaryClick,
  });

  // Update Markers
  useEffect(() => {
    if (!map || !isReady) return;
    map.clearMarkers();

    markers.forEach((m) => {
      map.addMarker({
        id: m.id,
        lat: m.latitude || m.lat,
        lng: m.longitude || m.lng,
        title: m.title,
        category: m.category,
        priority: m.priority,
        severity: m.severity,
        status: m.status,
        trackingNumber: m.trackingNumber,
        isSelected: String(m.id) === String(selectedMarkerId),
        data: m,
      });
    });
  }, [map, isReady, markers, selectedMarkerId]);

  // Update Ward Boundaries
  useEffect(() => {
    if (!map || !isReady) return;
    if (showBoundaries && wards.length > 0) {
      map.renderBoundaries(wards);
    } else {
      map.clearBoundaries();
    }
  }, [map, isReady, wards, showBoundaries]);

  // Update Heatmap
  useEffect(() => {
    if (!map || !isReady) return;
    if (showHeatmap && markers.length > 0) {
      const points = markers.map((m) => {
        let weight = 0.5;
        if (m.severity === 'CRITICAL') weight = 1.0;
        else if (m.severity === 'HIGH') weight = 0.8;
        else if (m.severity === 'MEDIUM') weight = 0.5;
        else weight = 0.3;

        return {
          lat: m.latitude || m.lat,
          lng: m.longitude || m.lng,
          weight,
        };
      });
      map.renderHeatmap(points);
    } else {
      map.clearHeatmap();
    }
  }, [map, isReady, markers, showHeatmap]);

  // Update Center if changed externally
  useEffect(() => {
    if (!map || !isReady || !center) return;
    map.setCenter(center[0], center[1], zoom);
  }, [map, isReady, center, zoom]);

  const handleZoomIn = () => {
    if (!map) return;
    map.setCenter(map.getCenter()[0], map.getCenter()[1], map.getZoom() + 1);
  };

  const handleZoomOut = () => {
    if (!map) return;
    map.setCenter(map.getCenter()[0], map.getCenter()[1], map.getZoom() - 1);
  };

  const handleReset = () => {
    if (!map) return;
    map.setCenter(17.385044, 78.486671, 13);
  };

  return (
    <div className={`relative rounded-2xl overflow-hidden border border-slate-200 shadow-sm ${className}`}>
      {/* Map Canvas Mount Point */}
      <div ref={containerRef} className="w-full h-full" />

      {/* Floating Zoom & View Controls */}
      <div className="absolute top-3 right-3 flex flex-col gap-1.5 z-20">
        <button
          type="button"
          onClick={handleZoomIn}
          className="w-8 h-8 rounded-lg bg-white/95 text-slate-800 shadow-md border border-slate-200 font-bold flex items-center justify-center hover:bg-slate-100 transition-colors cursor-pointer"
          aria-label="Zoom in"
          title="Zoom In"
        >
          +
        </button>
        <button
          type="button"
          onClick={handleZoomOut}
          className="w-8 h-8 rounded-lg bg-white/95 text-slate-800 shadow-md border border-slate-200 font-bold flex items-center justify-center hover:bg-slate-100 transition-colors cursor-pointer"
          aria-label="Zoom out"
          title="Zoom Out"
        >
          −
        </button>
        <button
          type="button"
          onClick={handleReset}
          className="w-8 h-8 rounded-lg bg-white/95 text-slate-800 shadow-md border border-slate-200 text-xs font-bold flex items-center justify-center hover:bg-slate-100 transition-colors cursor-pointer"
          aria-label="Reset map view"
          title="Center View"
        >
          ⌖
        </button>
      </div>

      {/* Heatmap Legend (when heatmap is on) */}
      {showHeatmap && (
        <div className="absolute bottom-3 left-3 bg-white/90 backdrop-blur-xs px-3 py-1.5 rounded-lg border border-slate-200 text-[10px] font-semibold text-slate-700 shadow-xs z-20 flex items-center gap-2">
          <span>Density:</span>
          <div className="w-20 h-2 rounded-full bg-gradient-to-r from-yellow-400 via-orange-500 to-red-600" />
          <span className="text-red-600 font-bold">High</span>
        </div>
      )}
    </div>
  );
};

export default InteractiveMap;
