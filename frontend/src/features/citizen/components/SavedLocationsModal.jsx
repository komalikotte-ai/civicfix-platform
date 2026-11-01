import React, { useState, useEffect } from 'react';
import Modal from '../../../components/common/Modal';
import Button from '../../../components/common/Button';
import Input from '../../../components/common/Input';
import Select from '../../../components/common/Select';
import savedLocationService from '../../../services/savedLocationService';
import organizationService from '../../../services/organizationService';

export const SavedLocationsModal = ({ isOpen, onClose, userId = 'current' }) => {
  const [locations, setLocations] = useState([]);
  const [wards, setWards] = useState([]);
  const [isAdding, setIsAdding] = useState(false);
  const [formData, setFormData] = useState({
    label: '',
    addressLine: '',
    landmark: '',
    wardId: '',
    latitude: 17.385044,
    longitude: 78.486671,
    isDefault: false,
  });
  const [error, setError] = useState('');

  const loadData = () => {
    const list = savedLocationService.getSavedLocations(userId);
    setLocations(list);
  };

  useEffect(() => {
    if (isOpen) {
      loadData();
      organizationService
        .getWards()
        .then((wList) =>
          setWards(
            (Array.isArray(wList) ? wList : []).map((w) => ({
              value: w.id,
              label: `Ward #${w.wardNumber} - ${w.name}`,
            }))
          )
        )
        .catch(() => {});
    }
  }, [isOpen, userId]);

  const handleSave = (e) => {
    e.preventDefault();
    if (!formData.label || !formData.addressLine || !formData.wardId) {
      setError('Please provide a label, ward, and address line.');
      return;
    }

    const selectedWard = wards.find((w) => String(w.value) === String(formData.wardId));
    savedLocationService.saveLocation(userId, {
      ...formData,
      wardId: Number(formData.wardId),
      wardName: selectedWard ? selectedWard.label : `Ward #${formData.wardId}`,
    });

    setIsAdding(false);
    setFormData({
      label: '',
      addressLine: '',
      landmark: '',
      wardId: '',
      latitude: 17.385044,
      longitude: 78.486671,
      isDefault: false,
    });
    setError('');
    loadData();
  };

  const handleDelete = (id) => {
    savedLocationService.deleteLocation(userId, id);
    loadData();
  };

  const handleSetDefault = (id) => {
    savedLocationService.setDefaultLocation(userId, id);
    loadData();
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Manage Saved Reporting Locations" size="lg">
      <div className="flex flex-col gap-4">
        <div className="flex items-center justify-between">
          <p className="text-xs text-slate-500">
            Save frequent locations (Home, Work, Parents' home) to auto-fill civic grievance reports in 1 click.
          </p>
          {!isAdding && (
            <Button size="sm" variant="primary" onClick={() => setIsAdding(true)}>
              + Add Location
            </Button>
          )}
        </div>

        {error && <div className="p-2.5 rounded bg-red-50 text-red-600 text-xs">{error}</div>}

        {/* Add Location Form */}
        {isAdding && (
          <form onSubmit={handleSave} className="p-4 rounded-xl border border-blue-200 bg-blue-50/40 flex flex-col gap-3">
            <h4 className="text-xs font-bold text-blue-900 uppercase tracking-wider">
              New Saved Location
            </h4>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <Input
                label="Location Label / Nickname"
                value={formData.label}
                onChange={(e) => setFormData({ ...formData, label: e.target.value })}
                placeholder="e.g. Home, Office, Park"
                required
              />

              <Select
                label="Municipal Ward"
                value={formData.wardId}
                onChange={(e) => setFormData({ ...formData, wardId: e.target.value })}
                options={wards}
                placeholder="Select Ward"
                required
              />
            </div>

            <Input
              label="Street Address / House No."
              value={formData.addressLine}
              onChange={(e) => setFormData({ ...formData, addressLine: e.target.value })}
              placeholder="e.g. Flat 301, Lakeview Heights, Main Road"
              required
            />

            <Input
              label="Landmark (Optional)"
              value={formData.landmark}
              onChange={(e) => setFormData({ ...formData, landmark: e.target.value })}
              placeholder="e.g. Opposite Water Tank"
            />

            <div className="flex items-center justify-between pt-2">
              <label className="flex items-center gap-2 text-xs text-slate-700 cursor-pointer">
                <input
                  type="checkbox"
                  checked={formData.isDefault}
                  onChange={(e) => setFormData({ ...formData, isDefault: e.target.checked })}
                  className="rounded border-slate-300 text-blue-600 focus:ring-blue-500"
                />
                <span>Set as primary default location</span>
              </label>

              <div className="flex gap-2">
                <Button type="button" size="sm" variant="ghost" onClick={() => setIsAdding(false)}>
                  Cancel
                </Button>
                <Button type="submit" size="sm" variant="success">
                  Save Location
                </Button>
              </div>
            </div>
          </form>
        )}

        {/* Saved Locations List */}
        <div className="space-y-3">
          {locations.length === 0 ? (
            <div className="py-8 text-center text-xs text-slate-500 bg-slate-50 rounded-xl border border-dashed border-slate-200">
              No saved locations yet. Click "+ Add Location" to register your home or office address.
            </div>
          ) : (
            locations.map((loc) => (
              <div
                key={loc.id}
                className={`p-3.5 rounded-xl border flex flex-col sm:flex-row sm:items-center justify-between gap-3 ${
                  loc.isDefault ? 'bg-blue-50/30 border-blue-200 shadow-xs' : 'bg-white border-slate-200'
                }`}
              >
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-bold text-sm text-slate-900">{loc.label}</span>
                    {loc.isDefault && (
                      <span className="px-1.5 py-0.5 rounded text-[10px] font-bold bg-blue-100 text-blue-800">
                        Default
                      </span>
                    )}
                  </div>
                  <div className="text-xs text-slate-700 mt-0.5">{loc.addressLine}</div>
                  <div className="text-[11px] text-slate-500 mt-0.5">
                    {loc.wardName || `Ward #${loc.wardId}`}
                    {loc.landmark && ` • Landmark: ${loc.landmark}`}
                  </div>
                </div>

                <div className="flex items-center gap-2 self-end sm:self-center">
                  {!loc.isDefault && (
                    <Button
                      size="sm"
                      variant="ghost"
                      onClick={() => handleSetDefault(loc.id)}
                      className="text-xs text-slate-600"
                    >
                      Make Default
                    </Button>
                  )}
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() => handleDelete(loc.id)}
                    className="text-xs text-red-600 hover:bg-red-50"
                  >
                    Delete
                  </Button>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </Modal>
  );
};

export default SavedLocationsModal;
