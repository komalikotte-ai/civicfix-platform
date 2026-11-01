import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { complaintService } from '../../services/complaintService';
import { organizationService } from '../../services/organizationService';
import { useAuth } from '../../hooks/useAuth';
import Card from '../../components/common/Card';
import Input from '../../components/common/Input';
import Select from '../../components/common/Select';
import Textarea from '../../components/common/Textarea';
import Button from '../../components/common/Button';
import Alert from '../../components/common/Alert';
import Badge from '../../components/common/Badge';
import AttachmentUpload from './components/AttachmentUpload';
import LocationPickerPlaceholder from './components/LocationPickerPlaceholder';
import ComplaintConfirmationModal from './components/ComplaintConfirmationModal';

const SEVERITIES = [
  {
    value: 'LOW',
    label: 'Low',
    desc: 'Minor cosmetic or non-blocking civic inconvenience',
    color: 'border-slate-300 text-slate-700 bg-slate-50',
  },
  {
    value: 'MEDIUM',
    label: 'Medium',
    desc: 'Noticeable public issue affecting daily neighborhood routine',
    color: 'border-amber-300 text-amber-800 bg-amber-50/60',
  },
  {
    value: 'HIGH',
    label: 'High',
    desc: 'Significant hazard or major disruption requiring priority action',
    color: 'border-orange-400 text-orange-900 bg-orange-50/70',
  },
  {
    value: 'CRITICAL',
    label: 'Critical',
    desc: 'Immediate emergency or safety threat to public life and health',
    color: 'border-red-500 text-red-900 bg-red-50/80',
  },
];

const ESTIMATED_SLAS = {
  LOW: '5 - 7 Business Days',
  MEDIUM: '48 - 72 Hours',
  HIGH: '24 - 48 Hours',
  URGENT: 'Within 12 - 24 Hours (Emergency Response)',
};

export const CreateComplaintPage = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [categories, setCategories] = useState([]);
  const [wards, setWards] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loadingLookups, setLoadingLookups] = useState(true);

  // Form State
  const [formData, setFormData] = useState({
    wardId: user?.wardId || '',
    departmentId: '',
    category: '',
    severity: 'MEDIUM',
    priority: 'MEDIUM',
    title: '',
    description: '',
    addressLine: '',
    landmark: '',
    latitude: 17.385044,
    longitude: 78.486671,
    attachments: [],
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [createdComplaint, setCreatedComplaint] = useState(null);

  // Load Categories & Wards from real backend APIs
  useEffect(() => {
    const fetchMetadata = async () => {
      setLoadingLookups(true);
      try {
        const [catList, wardList, deptList] = await Promise.all([
          complaintService.getCategories().catch(() => []),
          organizationService.getWards().catch(() => []),
          organizationService.getDepartments().catch(() => []),
        ]);

        const formattedCats = (Array.isArray(catList) ? catList : []).map((c) => ({
          value: c.name || c,
          label: c.displayName || c.name || c,
          defaultPriority: c.defaultPriority || 'MEDIUM',
          defaultDepartmentCode: c.defaultDepartmentCode,
        }));

        setCategories(formattedCats);

        const formattedWards = (Array.isArray(wardList) ? wardList : []).map((w) => ({
          value: w.id,
          label: `Ward #${w.wardNumber} - ${w.name}`,
        }));
        setWards(formattedWards);

        const formattedDepts = (Array.isArray(deptList) ? deptList : []).map((d) => ({
          value: d.id,
          label: `${d.name} (${d.code})`,
        }));
        setDepartments(formattedDepts);

        // Pre-select first category if available
        if (formattedCats.length > 0 && !formData.category) {
          const firstCat = formattedCats[0];
          setFormData((prev) => ({
            ...prev,
            category: firstCat.value,
            priority: firstCat.defaultPriority,
          }));
        }
      } catch (err) {
        setError('Failed to load municipal categories and wards. Please check connection.');
      } finally {
        setLoadingLookups(false);
      }
    };

    fetchMetadata();
  }, [user]);

  // Handle Category Change -> Update calculated Priority
  const handleCategoryChange = (e) => {
    const selectedCatValue = e.target.value;
    const catObj = categories.find((c) => c.value === selectedCatValue);
    const calculatedPriority = calculatePriority(selectedCatValue, formData.severity, catObj?.defaultPriority);

    setFormData((prev) => ({
      ...prev,
      category: selectedCatValue,
      priority: calculatedPriority,
    }));
  };

  // Handle Severity Change -> Update calculated Priority
  const handleSeveritySelect = (severityVal) => {
    const catObj = categories.find((c) => c.value === formData.category);
    const calculatedPriority = calculatePriority(formData.category, severityVal, catObj?.defaultPriority);

    setFormData((prev) => ({
      ...prev,
      severity: severityVal,
      priority: calculatedPriority,
    }));
  };

  // Compute recommended priority based on Severity and Category
  const calculatePriority = (catName, severity, defaultPriority = 'MEDIUM') => {
    if (severity === 'CRITICAL') return 'URGENT';
    if (severity === 'HIGH') return 'HIGH';
    if (severity === 'LOW') return 'LOW';
    return defaultPriority || 'MEDIUM';
  };

  const handleInputChange = (e) => {
    setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleLocationUpdate = (locUpdates) => {
    setFormData((prev) => ({
      ...prev,
      ...locUpdates,
      wardId: locUpdates.wardId ? String(locUpdates.wardId) : prev.wardId,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.wardId) {
      setError('Please select a municipal ward.');
      return;
    }
    if (!formData.category) {
      setError('Please select a complaint category.');
      return;
    }
    if (!formData.title || formData.title.trim().length < 5) {
      setError('Please provide a title with at least 5 characters.');
      return;
    }
    if (!formData.description || formData.description.trim().length < 15) {
      setError('Please provide a detailed description (minimum 15 characters).');
      return;
    }
    if (!formData.addressLine || formData.addressLine.trim().length < 3) {
      setError('Please provide a valid street address or landmark.');
      return;
    }

    setLoading(true);
    setError('');
    try {
      const payload = {
        title: formData.title.trim(),
        description: formData.description.trim(),
        category: formData.category,
        severity: formData.severity,
        priority: formData.priority,
        wardId: Number(formData.wardId),
        departmentId: formData.departmentId ? Number(formData.departmentId) : null,
        addressLine: formData.addressLine.trim(),
        landmark: formData.landmark ? formData.landmark.trim() : null,
        latitude: formData.latitude,
        longitude: formData.longitude,
        source: 'CITIZEN_PORTAL',
        attachments: formData.attachments.map((a) => ({
          fileName: a.fileName,
          fileUrl: a.fileUrl,
          fileType: a.fileType,
          fileSize: a.fileSize,
        })),
      };

      const result = await complaintService.createComplaint(payload);
      setCreatedComplaint(result);
    } catch (err) {
      setError(err.message || 'Failed to lodge complaint. Please check your inputs and try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleResetForm = () => {
    setCreatedComplaint(null);
    setFormData({
      wardId: user?.wardId || (wards[0]?.value ? String(wards[0].value) : ''),
      departmentId: '',
      category: categories[0]?.value || '',
      severity: 'MEDIUM',
      priority: 'MEDIUM',
      title: '',
      description: '',
      addressLine: '',
      landmark: '',
      latitude: 17.385044,
      longitude: 78.486671,
      attachments: [],
    });
    setError('');
  };

  // If complaint was created successfully, render Confirmation Screen
  if (createdComplaint) {
    return <ComplaintConfirmationModal complaint={createdComplaint} onReset={handleResetForm} />;
  }

  return (
    <div className="max-w-4xl mx-auto flex flex-col gap-6 pb-12">
      {/* Header */}
      <div>
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-50 text-blue-700 text-xs font-semibold mb-2 border border-blue-100">
          📝 Official Redressal Submission
        </div>
        <h1 className="text-2xl font-black text-slate-900 tracking-tight">
          Lodge a Municipal Grievance
        </h1>
        <p className="text-xs text-slate-500 mt-1 max-w-2xl">
          Report municipal issues directly to ward administration. Track step-by-step resolution from inspection to field completion.
        </p>
      </div>

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}

      <form onSubmit={handleSubmit} className="flex flex-col gap-6">
        {/* Step 1: Category & Ward Information */}
        <Card title="1. Issue Classification & Jurisdiction">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Select
              label="Grievance Category"
              name="category"
              value={formData.category}
              onChange={handleCategoryChange}
              options={categories}
              placeholder="Select Category"
              required
            />

            <Select
              label="Municipal Ward"
              name="wardId"
              value={formData.wardId}
              onChange={handleInputChange}
              options={wards}
              placeholder="Select Ward"
              required
            />
          </div>

          <div className="mt-4">
            <Select
              label="Target Department (Optional / Auto-routed if left blank)"
              name="departmentId"
              value={formData.departmentId}
              onChange={handleInputChange}
              options={departments}
              placeholder="Auto-route by Category (Recommended)"
            />
          </div>
        </Card>

        {/* Step 2: Severity Selection & Priority Preview */}
        <Card title="2. Grievance Severity & Expected Resolution SLA">
          <div className="flex flex-col gap-3">
            <label className="text-xs font-semibold text-slate-700">
              Select Observed Severity Level <span className="text-red-500">*</span>
            </label>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
              {SEVERITIES.map((s) => {
                const isSelected = formData.severity === s.value;
                return (
                  <button
                    key={s.value}
                    type="button"
                    onClick={() => handleSeveritySelect(s.value)}
                    className={`p-3 rounded-xl border text-left transition-all cursor-pointer flex flex-col justify-between ${
                      isSelected
                        ? `ring-2 ring-blue-600 shadow-sm ${s.color}`
                        : 'border-slate-200 bg-white hover:border-slate-300'
                    }`}
                  >
                    <div>
                      <div className="flex items-center justify-between">
                        <span className="font-bold text-xs">{s.label}</span>
                        {isSelected && <span className="text-blue-600 font-bold">✓</span>}
                      </div>
                      <p className="text-[11px] text-slate-500 mt-1 leading-snug">
                        {s.desc}
                      </p>
                    </div>
                  </button>
                );
              })}
            </div>

            {/* Calculated Priority & SLA Preview Box */}
            <div className="mt-2 p-3.5 rounded-xl bg-blue-50/70 border border-blue-200 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <div className="flex items-center gap-3">
                <span className="text-xs font-semibold text-slate-600">Calculated Priority:</span>
                <Badge status={formData.priority}>{formData.priority}</Badge>
              </div>
              <div className="text-xs text-blue-900 font-medium">
                <span className="text-blue-600 font-bold mr-1">Target SLA:</span>
                {ESTIMATED_SLAS[formData.priority] || '48-72 Hours'}
              </div>
            </div>
          </div>
        </Card>

        {/* Step 3: Location Details & Interactive Placeholder */}
        <Card title="3. Location & Geotagging">
          <div className="flex flex-col gap-4">
            <LocationPickerPlaceholder
              latitude={formData.latitude}
              longitude={formData.longitude}
              addressLine={formData.addressLine}
              landmark={formData.landmark}
              wardId={formData.wardId}
              wards={wards}
              onLocationChange={handleLocationUpdate}
              userId={user?.id}
            />

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Input
                label="Street Address / House / Landmark"
                name="addressLine"
                value={formData.addressLine}
                onChange={handleInputChange}
                placeholder="e.g. Opposite Pillar 12, MG Road"
                required
              />

              <Input
                label="Specific Landmark / Physical Reference"
                name="landmark"
                value={formData.landmark}
                onChange={handleInputChange}
                placeholder="e.g. Behind Government School, Near Transformer"
              />
            </div>
          </div>
        </Card>

        {/* Step 4: Issue Narrative */}
        <Card title="4. Grievance Description">
          <div className="flex flex-col gap-4">
            <Input
              label="Brief Title / Summary"
              name="title"
              value={formData.title}
              onChange={handleInputChange}
              placeholder="e.g. Broken water pipeline flooding street near Market square"
              required
            />

            <div className="flex flex-col gap-1">
              <label className="text-xs font-semibold text-slate-700">
                Detailed Description <span className="text-red-500">*</span>
              </label>
              <Textarea
                name="description"
                rows={4}
                value={formData.description}
                onChange={handleInputChange}
                placeholder="Describe the exact civic issue, duration, safety impact, and any relevant details..."
                maxLength={2000}
                showCount
                required
              />
            </div>
          </div>
        </Card>

        {/* Step 5: Attachments UI */}
        <Card title="5. Photographic Evidence & Documentation">
          <AttachmentUpload
            attachments={formData.attachments}
            onChange={(updater) =>
              setFormData((prev) => ({
                ...prev,
                attachments: typeof updater === 'function' ? updater(prev.attachments) : updater,
              }))
            }
          />
        </Card>

        {/* Submit Actions */}
        <div className="flex flex-col sm:flex-row items-center justify-between gap-3 p-4 bg-white rounded-xl border border-slate-200 shadow-xs">
          <Button
            type="button"
            variant="ghost"
            onClick={() => navigate('/citizen')}
            disabled={loading}
          >
            Cancel & Return to Dashboard
          </Button>

          <Button
            type="submit"
            variant="success"
            size="lg"
            loading={loading}
            className="w-full sm:w-auto font-bold shadow-md"
          >
            Submit Grievance
          </Button>
        </div>
      </form>
    </div>
  );
};

export default CreateComplaintPage;
