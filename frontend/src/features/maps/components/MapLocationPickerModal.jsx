import React, { useState, useEffect } from 'react';
import Modal from '../../../components/common/Modal.jsx';
import Button from '../../../components/common/Button.jsx';
import Alert from '../../../components/common/Alert.jsx';
import InteractiveMap from './InteractiveMap.jsx';
import AddressSearchBar from './AddressSearchBar.jsx';
import DuplicateWarningBanner from './DuplicateWarningBanner.jsx';
import useGeolocation, { GEOLOCATION_STATUS } from '../hooks/useGeolocation.js';
import geocodingService from '../../../services/geocodingService.js';
import mapService from '../../../services/mapService.js';
import { findPotentialDuplicates } from '../utils/geoUtils.js';

export const MapLocationPickerModal = ({
  isOpen,
  onClose,
  initialCoords = { latitude: 17.385044, longitude: 78.486671 },
  category = '',
  onConfirmLocation,
}) => {
  const [selectedPin, setSelectedPin] = useState(initialCoords);
  const [resolvedInfo, setResolvedInfo] = useState(null);
  const [wards, setWards] = useState([]);
  const [existingComplaints, setExistingComplaints] = useState([]);
  const [potentialDuplicates, setPotentialDuplicates] = useState([]);
  const [resolving, setResolving] = useState(false);

  const { coords: gpsCoords, status: gpsStatus, errorMessage: gpsError, getCurrentLocation, isLoading: gpsLoading } =
    useGeolocation();

  // Load wards and existing complaints for spatial duplicate checking
  useEffect(() => {
    if (isOpen) {
      mapService.getWards().then((wList) => setWards(Array.isArray(wList) ? wList : [])).catch(() => {});
      mapService.getGrievancePins().then((pins) => setExistingComplaints(pins || [])).catch(() => {});
    }
  }, [isOpen]);

  // When GPS succeeds, update selected pin
  useEffect(() => {
    if (gpsStatus === GEOLOCATION_STATUS.SUCCESS && gpsCoords) {
      setSelectedPin(gpsCoords);
      handleResolveLocation(gpsCoords.latitude, gpsCoords.longitude);
    }
  }, [gpsStatus, gpsCoords]);

  // Reverse geocode when pin changes
  const handleResolveLocation = async (lat, lng) => {
    setResolving(true);
    try {
      const info = await geocodingService.reverseGeocode(lat, lng);
      setResolvedInfo(info);

      // Check for duplicates in proximity
      const duplicates = findPotentialDuplicates({ latitude: lat, longitude: lng, category }, existingComplaints);
      setPotentialDuplicates(duplicates);
    } finally {
      setResolving(false);
    }
  };

  useEffect(() => {
    if (isOpen && initialCoords) {
      handleResolveLocation(initialCoords.latitude, initialCoords.longitude);
    }
  }, [isOpen, initialCoords]);

  const handleMapClick = ({ lat, lng }) => {
    setSelectedPin({ latitude: lat, longitude: lng });
    handleResolveLocation(lat, lng);
  };

  const handleSelectFromSearch = (searchItem) => {
    const lat = searchItem.latitude;
    const lng = searchItem.longitude;
    setSelectedPin({ latitude: lat, longitude: lng });
    handleResolveLocation(lat, lng);
  };

  const handleConfirm = () => {
    onConfirmLocation({
      latitude: selectedPin.latitude,
      longitude: selectedPin.longitude,
      addressLine: resolvedInfo?.addressLine || 'Municipal Street Location',
      landmark: resolvedInfo?.landmark || '',
      wardId: resolvedInfo?.wardId || 1,
      wardName: resolvedInfo?.wardName || 'Ward #1',
    });
    onClose();
  };

  if (!isOpen) return null;

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Select Grievance Location on Map" size="xl">
      <div className="flex flex-col gap-4 text-xs">
        {/* Top Search & GPS Bar */}
        <div className="flex flex-col sm:flex-row items-center justify-between gap-3">
          <AddressSearchBar onSelectLocation={handleSelectFromSearch} placeholder="Search address, landmark, or ward..." />

          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={() => getCurrentLocation()}
            loading={gpsLoading}
            className="w-full sm:w-auto text-xs whitespace-nowrap"
          >
            🎯 Use GPS Location
          </Button>
        </div>

        {/* GPS Error Handling */}
        {gpsError && (
          <Alert
            type={gpsStatus === GEOLOCATION_STATUS.PERMISSION_DENIED ? 'warning' : 'error'}
            message={gpsError}
          />
        )}

        {/* Interactive Map */}
        <div className="relative">
          <InteractiveMap
            center={[selectedPin.latitude, selectedPin.longitude]}
            zoom={15}
            markers={[
              {
                id: 'target-selection-pin',
                latitude: selectedPin.latitude,
                longitude: selectedPin.longitude,
                title: 'Selected Grievance Location',
                category: category || 'POTHOLE',
                severity: 'CRITICAL',
                isSelected: true,
              },
            ]}
            wards={wards}
            onMapClick={handleMapClick}
            className="h-80 w-full"
          />

          <div className="absolute top-3 left-3 bg-white/90 backdrop-blur-xs px-2.5 py-1 rounded-lg border border-slate-200 text-[11px] font-semibold text-slate-700 shadow-xs pointer-events-none">
            💡 Click anywhere on map to reposition pin
          </div>
        </div>

        {/* Potential Duplicate Warning (Consumes backend / spatial match) */}
        {potentialDuplicates.length > 0 && (
          <DuplicateWarningBanner
            potentialDuplicates={potentialDuplicates}
            onProceedAnyway={() => setPotentialDuplicates([])}
            onDismiss={() => setPotentialDuplicates([])}
          />
        )}

        {/* Resolved Address Readout Card */}
        <div className="p-3.5 bg-slate-50 border border-slate-200 rounded-xl flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div>
            <div className="font-bold text-slate-900 text-sm">
              {resolving ? 'Resolving address...' : resolvedInfo?.addressLine || 'Coordinates Selected'}
            </div>
            <div className="text-slate-500 text-[11px] mt-0.5">
              {resolvedInfo?.wardName || 'Ward Area'} • Landmark: {resolvedInfo?.landmark || 'None'}
            </div>
            <div className="font-mono text-[10px] text-slate-400 mt-1">
              LAT: {selectedPin.latitude?.toFixed(6)} | LNG: {selectedPin.longitude?.toFixed(6)}
            </div>
          </div>

          <div className="flex gap-2">
            <Button variant="ghost" size="sm" onClick={onClose}>
              Cancel
            </Button>
            <Button variant="success" size="sm" onClick={handleConfirm} disabled={resolving} className="font-bold">
              ✓ Confirm Location
            </Button>
          </div>
        </div>
      </div>
    </Modal>
  );
};

export default MapLocationPickerModal;
