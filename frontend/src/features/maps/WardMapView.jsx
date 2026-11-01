import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { mapService } from '../../services/mapService.js';
import { complaintService } from '../../services/complaintService.js';
import Card from '../../components/common/Card.jsx';
import Badge from '../../components/common/Badge.jsx';
import Button from '../../components/common/Button.jsx';
import Select from '../../components/common/Select.jsx';
import LoadingState from '../../components/common/LoadingState.jsx';
import ErrorState from '../../components/common/ErrorState.jsx';
import EmptyState from '../../components/common/EmptyState.jsx';
import InteractiveMap from './components/InteractiveMap.jsx';
import AddressSearchBar from './components/AddressSearchBar.jsx';
import ComplaintMapCard from './components/ComplaintMapCard.jsx';
import NearbyComplaintsDrawer from './components/NearbyComplaintsDrawer.jsx';
import DuplicateWarningBanner from './components/DuplicateWarningBanner.jsx';
import useGeolocation, { GEOLOCATION_STATUS } from './hooks/useGeolocation.js';
import { haversineDistance, findPotentialDuplicates } from './utils/geoUtils.js';

export const WardMapView = () => {
  const navigate = useNavigate();

  const [wards, setWards] = useState([]);
  const [pins, setPins] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Filters
  const [selectedWardId, setSelectedWardId] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [selectedPriority, setSelectedPriority] = useState('');
  const [searchQuery, setSearchQuery] = useState('');

  // Map state
  const [center, setCenter] = useState([17.385044, 78.486671]);
  const [zoom, setZoom] = useState(13);
  const [selectedPin, setSelectedPin] = useState(null);
  const [showHeatmap, setShowHeatmap] = useState(false);
  const [showBoundaries, setShowBoundaries] = useState(true);
  const [viewMode, setViewMode] = useState('split'); // 'split' | 'map-only' | 'list-only'

  // GPS Hook
  const { coords: gpsCoords, status: gpsStatus, errorMessage: gpsError, getCurrentLocation, isLoading: gpsLoading } =
    useGeolocation();

  const loadData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [wardsData, pinsData, catsData] = await Promise.all([
        mapService.getWards(),
        mapService.getGrievancePins(),
        complaintService.getCategories().catch(() => []),
      ]);

      setWards(Array.isArray(wardsData) ? wardsData : []);
      setPins(Array.isArray(pinsData) ? pinsData : []);

      const formattedCats = [
        { value: '', label: 'All Categories' },
        ...(Array.isArray(catsData) ? catsData : []).map((c) => ({
          value: c.name || c,
          label: c.displayName || c.name || c,
        })),
      ];
      setCategories(formattedCats);
    } catch (err) {
      setError(err.message || 'Failed to load spatial map data.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  // Handle GPS location detection
  useEffect(() => {
    if (gpsStatus === GEOLOCATION_STATUS.SUCCESS && gpsCoords) {
      setCenter([gpsCoords.latitude, gpsCoords.longitude]);
      setZoom(15);
    }
  }, [gpsStatus, gpsCoords]);

  // Filter Pins based on selections
  const filteredPins = useMemo(() => {
    return pins.filter((p) => {
      if (selectedWardId && String(p.wardId) !== String(selectedWardId)) return false;
      if (selectedCategory && p.category?.toUpperCase() !== selectedCategory.toUpperCase()) return false;
      if (selectedPriority && p.priority?.toUpperCase() !== selectedPriority.toUpperCase()) return false;
      if (searchQuery) {
        const q = searchQuery.toLowerCase();
        const titleMatch = p.title?.toLowerCase().includes(q);
        const refMatch = p.trackingNumber?.toLowerCase().includes(q);
        const addrMatch = p.addressLine?.toLowerCase().includes(q);
        if (!titleMatch && !refMatch && !addrMatch) return false;
      }
      return true;
    });
  }, [pins, selectedWardId, selectedCategory, selectedPriority, searchQuery]);

  // Calculate nearby complaints to currently selected pin or map center
  const nearbyComplaints = useMemo(() => {
    const targetLat = selectedPin ? (selectedPin.latitude || selectedPin.lat) : center[0];
    const targetLng = selectedPin ? (selectedPin.longitude || selectedPin.lng) : center[1];

    return pins
      .map((p) => ({
        ...p,
        distance: haversineDistance(targetLat, targetLng, p.latitude || p.lat, p.longitude || p.lng),
      }))
      .filter((p) => (!selectedPin || String(p.id) !== String(selectedPin.id)) && p.distance <= 1000)
      .sort((a, b) => a.distance - b.distance);
  }, [pins, selectedPin, center]);

  // Potential duplicate complaints
  const potentialDuplicates = useMemo(() => {
    if (!selectedPin) return [];
    return findPotentialDuplicates(selectedPin, pins, 200).filter(
      (d) => String(d.id) !== String(selectedPin.id)
    );
  }, [selectedPin, pins]);

  const handleMarkerClick = (marker) => {
    setSelectedPin(marker.data || marker);
    setCenter([marker.lat, marker.lng]);
  };

  const handleSelectFromList = (complaint) => {
    setSelectedPin(complaint);
    if (complaint.latitude && complaint.longitude) {
      setCenter([complaint.latitude, complaint.longitude]);
      setZoom(15);
    }
  };

  const handleSelectLocation = (loc) => {
    setCenter([loc.latitude, loc.longitude]);
    setZoom(15);
    if (loc.wardId) {
      setSelectedWardId(String(loc.wardId));
    }
  };

  const handleBoundaryClick = (ward) => {
    setSelectedWardId(String(ward.id));
    if (ward.centerLat && ward.centerLng) {
      setCenter([ward.centerLat, ward.centerLng]);
      setZoom(14);
    }
  };

  const wardOptions = [
    { value: '', label: 'All Wards' },
    ...wards.map((w) => ({
      value: String(w.id),
      label: `Ward #${w.wardNumber || w.id} - ${w.name}`,
    })),
  ];

  const priorityOptions = [
    { value: '', label: 'All Priorities' },
    { value: 'LOW', label: 'Low Priority' },
    { value: 'MEDIUM', label: 'Medium Priority' },
    { value: 'HIGH', label: 'High Priority' },
    { value: 'URGENT', label: 'Urgent Priority' },
  ];

  return (
    <div className="flex flex-col gap-5 max-w-7xl mx-auto pb-12">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-blue-50 text-blue-700 text-xs font-semibold mb-1 border border-blue-100">
            🗺️ Geographic Information System (GIS)
          </div>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight">
            Municipal Ward & Grievance Map
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Spatial monitoring of civic reports, complaint clusters, ward boundaries, and density heatmaps.
          </p>
        </div>

        {/* View mode toggle */}
        <div className="flex items-center gap-2">
          <div className="flex items-center text-xs border border-slate-300 rounded-xl overflow-hidden bg-white shadow-xs">
            <button
              type="button"
              onClick={() => setViewMode('split')}
              className={`px-3 py-1.5 font-semibold transition-colors cursor-pointer ${
                viewMode === 'split' ? 'bg-blue-600 text-white' : 'text-slate-700 hover:bg-slate-100'
              }`}
            >
              Split View
            </button>
            <button
              type="button"
              onClick={() => setViewMode('map-only')}
              className={`px-3 py-1.5 font-semibold transition-colors cursor-pointer ${
                viewMode === 'map-only' ? 'bg-blue-600 text-white' : 'text-slate-700 hover:bg-slate-100'
              }`}
            >
              Map Only
            </button>
            <button
              type="button"
              onClick={() => setViewMode('list-only')}
              className={`px-3 py-1.5 font-semibold transition-colors cursor-pointer ${
                viewMode === 'list-only' ? 'bg-blue-600 text-white' : 'text-slate-700 hover:bg-slate-100'
              }`}
            >
              List Only
            </button>
          </div>

          <Button variant="outline" size="sm" onClick={loadData}>
            ↻ Refresh
          </Button>
        </div>
      </div>

      {/* Control Bar: Address Search & Filters */}
      <div className="p-4 bg-white rounded-2xl border border-slate-200 shadow-xs flex flex-col gap-3">
        <div className="flex flex-col lg:flex-row items-center justify-between gap-3">
          <div className="w-full lg:w-96">
            <AddressSearchBar onSelectLocation={handleSelectLocation} />
          </div>

          <div className="flex flex-wrap items-center gap-2 w-full lg:w-auto">
            {/* GPS Button */}
            <Button
              variant="outline"
              size="sm"
              onClick={() => getCurrentLocation()}
              loading={gpsLoading}
              className="text-xs"
            >
              🎯 My Location
            </Button>

            {/* Heatmap Toggle */}
            <Button
              variant={showHeatmap ? 'primary' : 'outline'}
              size="sm"
              onClick={() => setShowHeatmap((prev) => !prev)}
              className="text-xs font-semibold"
            >
              {showHeatmap ? '🔥 Heatmap: ON' : '🔥 Heatmap: OFF'}
            </Button>

            {/* Boundaries Toggle */}
            <Button
              variant={showBoundaries ? 'secondary' : 'outline'}
              size="sm"
              onClick={() => setShowBoundaries((prev) => !prev)}
              className="text-xs font-semibold"
            >
              {showBoundaries ? '🏛️ Wards: Visible' : '🏛️ Wards: Hidden'}
            </Button>
          </div>
        </div>

        {/* Filter Dropdowns Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 pt-2 border-t border-slate-100 text-xs">
          <Select
            options={wardOptions}
            value={selectedWardId}
            onChange={(e) => setSelectedWardId(e.target.value)}
          />

          <Select
            options={categories}
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value)}
          />

          <Select
            options={priorityOptions}
            value={selectedPriority}
            onChange={(e) => setSelectedPriority(e.target.value)}
          />
        </div>
      </div>

      {/* GPS Warning if permission denied or unavailable */}
      {gpsError && (
        <div className="p-3 bg-amber-50 border border-amber-200 text-amber-800 rounded-xl text-xs flex items-center justify-between">
          <span>⚠️ {gpsError}</span>
        </div>
      )}

      {/* Main Map & Synchronized List Section */}
      {loading ? (
        <LoadingState message="Loading geospatial vector layers and incident points..." />
      ) : error ? (
        <ErrorState message={error} onRetry={loadData} />
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
          {/* Map Container */}
          {(viewMode === 'split' || viewMode === 'map-only') && (
            <div className={`${viewMode === 'map-only' ? 'lg:col-span-12' : 'lg:col-span-8'} flex flex-col gap-4`}>
              <div className="relative">
                <InteractiveMap
                  center={center}
                  zoom={zoom}
                  markers={filteredPins}
                  selectedMarkerId={selectedPin?.id}
                  wards={wards}
                  showHeatmap={showHeatmap}
                  showBoundaries={showBoundaries}
                  onMarkerClick={handleMarkerClick}
                  onBoundaryClick={handleBoundaryClick}
                  className="h-[520px] w-full"
                />

                {/* Selected Complaint Floating Card */}
                {selectedPin && (
                  <ComplaintMapCard complaint={selectedPin} onClose={() => setSelectedPin(null)} />
                )}
              </div>

              {/* Duplicate Report Warning UI (consumes backend / spatial checks) */}
              {selectedPin && potentialDuplicates.length > 0 && (
                <DuplicateWarningBanner
                  potentialDuplicates={potentialDuplicates}
                  onViewExisting={(dup) => {
                    setSelectedPin(dup);
                    if (dup.latitude && dup.longitude) setCenter([dup.latitude, dup.longitude]);
                  }}
                  onProceedAnyway={() => setSelectedPin(null)}
                />
              )}
            </div>
          )}

          {/* Synchronized List & Proximity Drawer */}
          {(viewMode === 'split' || viewMode === 'list-only') && (
            <div className={`${viewMode === 'list-only' ? 'lg:col-span-12' : 'lg:col-span-4'} flex flex-col gap-4`}>
              {/* Nearby Complaints Drawer */}
              <Card title="Spatial Proximity Inspection">
                <NearbyComplaintsDrawer
                  nearbyComplaints={nearbyComplaints}
                  selectedComplaintId={selectedPin?.id}
                  onSelectComplaint={handleSelectFromList}
                />
              </Card>

              {/* Synchronized Complaints List */}
              <Card title={`Active Grievances (${filteredPins.length})`}>
                {filteredPins.length === 0 ? (
                  <EmptyState
                    title="No complaints in this zone"
                    description="No civic reports match your currently active filters."
                    actionLabel="Reset Filters"
                    onAction={() => {
                      setSelectedWardId('');
                      setSelectedCategory('');
                      setSelectedPriority('');
                      setSearchQuery('');
                    }}
                  />
                ) : (
                  <div className="space-y-2 max-h-[340px] overflow-y-auto pr-1 divide-y divide-slate-100">
                    {filteredPins.map((item) => {
                      const isSelected = selectedPin && String(selectedPin.id) === String(item.id);
                      return (
                        <button
                          key={item.id}
                          type="button"
                          onClick={() => handleSelectFromList(item)}
                          className={`w-full text-left p-3 rounded-xl border transition-all cursor-pointer flex flex-col gap-1 ${
                            isSelected
                              ? 'bg-blue-50 border-blue-400 ring-2 ring-blue-400 shadow-xs'
                              : 'bg-white border-slate-200 hover:border-slate-300'
                          }`}
                        >
                          <div className="flex items-center justify-between">
                            <span className="font-mono text-xs font-bold text-blue-600">
                              {item.trackingNumber || `CF-${item.id}`}
                            </span>
                            <Badge status={item.status}>{item.status}</Badge>
                          </div>
                          <div className="font-semibold text-xs text-slate-900 truncate">
                            {item.title}
                          </div>
                          <div className="flex items-center justify-between text-[11px] text-slate-500 mt-0.5">
                            <span>{item.category || 'General'}</span>
                            <span className="font-medium text-slate-700">
                              {item.wardName || `Ward #${item.wardId || '1'}`}
                            </span>
                          </div>
                        </button>
                      );
                    })}
                  </div>
                )}
              </Card>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default WardMapView;
