import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { complaintService } from '../../services/complaintService';
import { organizationService } from '../../services/organizationService';
import { useAuth } from '../../hooks/useAuth';
import Card from '../../components/common/Card';
import Input from '../../components/common/Input';
import Select from '../../components/common/Select';
import Button from '../../components/common/Button';
import Alert from '../../components/common/Alert';

const CATEGORIES = [
  { value: 'Pothole & Road Damage', label: 'Pothole & Road Damage' },
  { value: 'Garbage & Solid Waste', label: 'Garbage & Solid Waste' },
  { value: 'Streetlight Failure', label: 'Streetlight Failure' },
  { value: 'Water Leakage / Contamination', label: 'Water Leakage / Contamination' },
  { value: 'Sewage Overflow', label: 'Sewage Overflow' },
  { value: 'Illegal Construction / Encroachment', label: 'Illegal Construction / Encroachment' },
  { value: 'Public Health & Sanitation', label: 'Public Health & Sanitation' },
  { value: 'Other Municipal Issue', label: 'Other Municipal Issue' },
];

const PRIORITIES = [
  { value: 'LOW', label: 'Low (General Inquiry / Non-urgent)' },
  { value: 'MEDIUM', label: 'Medium (Standard civic complaint)' },
  { value: 'HIGH', label: 'High (Significant inconvenience / safety concern)' },
  { value: 'CRITICAL', label: 'Critical (Immediate public hazard / emergency)' },
];

export const CreateComplaintPage = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [wards, setWards] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loadingLookups, setLoadingLookups] = useState(true);

  const [formData, setFormData] = useState({
    wardId: user?.wardId || '',
    departmentId: '',
    category: '',
    priority: 'MEDIUM',
    title: '',
    description: '',
    addressLine: '',
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchMetadata = async () => {
      const orgId = user?.organizationId || 1;
      try {
        const [wList, dList] = await Promise.all([
          organizationService.getWards(orgId).catch(() => []),
          organizationService.getDepartments(orgId).catch(() => []),
        ]);
        setWards(wList.map((w) => ({ value: w.id, label: `Ward #${w.wardNumber} - ${w.name}` })));
        setDepartments(dList.map((d) => ({ value: d.id, label: d.name })));
      } finally {
        setLoadingLookups(false);
      }
    };

    fetchMetadata();
  }, [user]);

  const handleChange = (e) => {
    setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.wardId || !formData.title || !formData.description || !formData.category) {
      setError('Please fill all mandatory fields (Ward, Title, Description, Category)');
      return;
    }

    setLoading(true);
    setError('');
    try {
      const created = await complaintService.createComplaint({
        ...formData,
        wardId: Number(formData.wardId),
        departmentId: formData.departmentId ? Number(formData.departmentId) : null,
      });
      navigate(`/complaints/${created.id}`);
    } catch (err) {
      setError(err.message || 'Failed to lodge complaint');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto flex flex-col gap-6">
      <div>
        <h2 className="text-xl font-bold text-gray-900">Lodge a Municipal Grievance</h2>
        <p className="text-xs text-gray-500 mt-1">
          Submit your complaint to city administration with location and details
        </p>
      </div>

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}

      <Card>
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Select
              label="Select Ward"
              name="wardId"
              value={formData.wardId}
              onChange={handleChange}
              options={wards}
              placeholder="Select your ward"
              required
            />

            <Select
              label="Department (Optional / Auto-triaged)"
              name="departmentId"
              value={formData.departmentId}
              onChange={handleChange}
              options={departments}
              placeholder="Select department (if known)"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Select
              label="Category"
              name="category"
              value={formData.category}
              onChange={handleChange}
              options={CATEGORIES}
              placeholder="Select complaint category"
              required
            />

            <Select
              label="Priority"
              name="priority"
              value={formData.priority}
              onChange={handleChange}
              options={PRIORITIES}
              required
            />
          </div>

          <Input
            label="Brief Title / Summary"
            name="title"
            value={formData.title}
            onChange={handleChange}
            placeholder="e.g. Deep pothole causing accidents near Market Gate"
            required
          />

          <div className="flex flex-col gap-1 w-full">
            <label className="text-xs font-semibold text-gray-700">
              Detailed Description <span className="text-red-500">*</span>
            </label>
            <textarea
              name="description"
              rows={4}
              value={formData.description}
              onChange={handleChange}
              placeholder="Provide exact details of the grievance..."
              className="px-3 py-2 text-sm rounded border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>

          <Input
            label="Landmark / Street Address"
            name="addressLine"
            value={formData.addressLine}
            onChange={handleChange}
            placeholder="e.g. Opposite Sector 4 Community Center, Main Road"
          />

          <div className="flex justify-end gap-3 pt-3 border-t border-gray-100">
            <Button variant="secondary" onClick={() => navigate(-1)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={loading}>
              Submit Grievance
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};

export default CreateComplaintPage;
