import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import workOrderService from '../../services/workOrderService';
import syncEngine from '../../services/syncEngine';
import SyncStatusBanner from './SyncStatusBanner';

export const JobExecutionPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [job, setJob] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  // Execution states
  const [timerRunning, setTimerRunning] = useState(false);
  const [elapsedSeconds, setElapsedSeconds] = useState(0);

  // Completion Form Fields
  const [completionNotes, setCompletionNotes] = useState('');
  const [actualCost, setActualCost] = useState('');
  const [beforePhotos, setBeforePhotos] = useState([]);
  const [afterPhotos, setAfterPhotos] = useState([]);
  const [materials, setMaterials] = useState([{ name: '', quantity: '', unit: 'units' }]);

  // Safety Checklist
  const [safetyChecklist, setSafetyChecklist] = useState({
    ppeEquipped: false,
    siteBarricaded: false,
    trafficDiverted: false,
    powerIsolated: false,
    gasChecked: false,
  });

  // Digital Acknowledgement
  const [acknowledgementName, setAcknowledgementName] = useState('');
  const [acknowledged, setAcknowledged] = useState(false);

  // Comments
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState('');

  // GPS Check-in status
  const [lastCheckin, setLastCheckin] = useState(null);

  // Load Job
  const loadJobData = async () => {
    setLoading(true);
    setErrorMessage('');

    // Try server first if online
    if (syncEngine.isOnline()) {
      try {
        const data = await workOrderService.getWorkOrderById(id);
        setJob(data);
        syncEngine.updateLocalJob(id, data);
        loadComments();
      } catch (err) {
        // Fall back to offline cache
        const cached = syncEngine.getCachedJob(id);
        if (cached) {
          setJob(cached);
        } else {
          setErrorMessage('Unable to load work order details.');
        }
      }
    } else {
      const cached = syncEngine.getCachedJob(id);
      if (cached) {
        setJob(cached);
      } else {
        setErrorMessage('Work order not found in offline storage.');
      }
    }

    // Load drafts and GPS check-ins
    const draft = syncEngine.getDraft(id);
    if (draft) {
      if (draft.completionNotes) setCompletionNotes(draft.completionNotes);
      if (draft.actualCost) setActualCost(draft.actualCost);
      if (draft.safetyChecklist) setSafetyChecklist(draft.safetyChecklist);
      if (draft.materials) setMaterials(draft.materials);
      if (draft.beforePhotos) setBeforePhotos(draft.beforePhotos);
      if (draft.afterPhotos) setAfterPhotos(draft.afterPhotos);
      if (draft.acknowledgementName) setAcknowledgementName(draft.acknowledgementName);
      if (draft.acknowledged) setAcknowledged(draft.acknowledged);
    }

    const checkins = syncEngine.getGpsCheckins(Number(id));
    if (checkins.length > 0) {
      setLastCheckin(checkins[checkins.length - 1]);
    }

    setLoading(false);
  };

  const loadComments = async () => {
    if (syncEngine.isOnline()) {
      try {
        const comms = await workOrderService.getComments(id, true);
        setComments(comms || []);
      } catch (e) {}
    }
  };

  useEffect(() => {
    loadJobData();
  }, [id]);

  // Timer effect for time tracking while IN_PROGRESS
  useEffect(() => {
    let interval = null;
    if (timerRunning) {
      interval = setInterval(() => {
        setElapsedSeconds((prev) => prev + 1);
      }, 1000);
    }
    return () => clearInterval(interval);
  }, [timerRunning]);

  // Auto-save draft on form input
  useEffect(() => {
    if (job) {
      syncEngine.saveDraft(id, {
        completionNotes,
        actualCost,
        safetyChecklist,
        materials,
        beforePhotos,
        afterPhotos,
        acknowledgementName,
        acknowledged,
      });
    }
  }, [completionNotes, actualCost, safetyChecklist, materials, beforePhotos, afterPhotos, acknowledgementName, acknowledged, id, job]);

  // GPS Check-in Handler
  const handleGpsCheckIn = () => {
    if (!navigator.geolocation) {
      alert('Geolocation is not supported by your browser.');
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const coords = {
          latitude: pos.coords.latitude,
          longitude: pos.coords.longitude,
          accuracy: pos.coords.accuracy,
        };
        const recorded = syncEngine.recordGpsCheckIn(Number(id), coords);
        setLastCheckin(recorded);
        setSuccessMessage(`GPS Check-in recorded at ${pos.coords.latitude.toFixed(6)}, ${pos.coords.longitude.toFixed(6)}`);
      },
      (err) => {
        alert('Could not retrieve GPS coordinates: ' + err.message);
      },
      { enableHighAccuracy: true, timeout: 10000 }
    );
  };

  // Job Actions with Online / Offline Handling
  const handleAcceptJob = async () => {
    setActionLoading(true);
    setErrorMessage('');
    if (syncEngine.isOnline()) {
      try {
        const updated = await workOrderService.acceptJob(id, 'Accepted from Field App');
        setJob(updated);
        syncEngine.updateLocalJob(id, updated);
        setSuccessMessage('Job accepted successfully.');
      } catch (err) {
        setErrorMessage(err.message || 'Failed to accept job');
      }
    } else {
      syncEngine.enqueueAction({
        action: 'ACCEPT',
        workOrderId: Number(id),
        expectedCurrentStatus: 'ASSIGNED',
        payload: { remarks: 'Accepted offline from Field App' },
      });
      setJob((prev) => ({ ...prev, status: 'ACCEPTED' }));
      setSuccessMessage('Offline: Job marked as accepted locally. Will sync when online.');
    }
    setActionLoading(false);
  };

  const handleStartWork = async () => {
    // Check safety checklist prior to start
    if (!safetyChecklist.ppeEquipped || !safetyChecklist.siteBarricaded) {
      alert('Please complete the mandatory safety checks (PPE Equipped & Site Barricaded) before commencing work.');
      return;
    }

    setActionLoading(true);
    setErrorMessage('');
    if (syncEngine.isOnline()) {
      try {
        const updated = await workOrderService.startJob(id, 'Work commenced on site');
        setJob(updated);
        syncEngine.updateLocalJob(id, updated);
        setTimerRunning(true);
        setSuccessMessage('Work started! Time tracking activated.');
      } catch (err) {
        setErrorMessage(err.message || 'Failed to start job');
      }
    } else {
      syncEngine.enqueueAction({
        action: 'START',
        workOrderId: Number(id),
        expectedCurrentStatus: job.status,
        payload: { remarks: 'Work commenced offline on site' },
      });
      setJob((prev) => ({ ...prev, status: 'IN_PROGRESS', actualStartDate: new Date().toISOString() }));
      setTimerRunning(true);
      setSuccessMessage('Offline: Work marked IN_PROGRESS locally. Will sync when online.');
    }
    setActionLoading(false);
  };

  const handlePauseWork = async () => {
    const reason = prompt('Please enter reason for pause (e.g. weather delay, material restock):');
    if (!reason) return;

    setActionLoading(true);
    if (syncEngine.isOnline()) {
      try {
        const updated = await workOrderService.pauseJob(id, reason);
        setJob(updated);
        syncEngine.updateLocalJob(id, updated);
        setTimerRunning(false);
        setSuccessMessage('Work order placed on hold.');
      } catch (err) {
        setErrorMessage(err.message || 'Failed to pause work');
      }
    } else {
      syncEngine.enqueueAction({
        action: 'PAUSE',
        workOrderId: Number(id),
        expectedCurrentStatus: 'IN_PROGRESS',
        payload: { reason },
      });
      setJob((prev) => ({ ...prev, status: 'ON_HOLD' }));
      setTimerRunning(false);
      setSuccessMessage('Offline: Work paused locally. Will sync when online.');
    }
    setActionLoading(false);
  };

  const handleResumeWork = async () => {
    setActionLoading(true);
    if (syncEngine.isOnline()) {
      try {
        const updated = await workOrderService.resumeJob(id, 'Work resumed on site');
        setJob(updated);
        syncEngine.updateLocalJob(id, updated);
        setTimerRunning(true);
        setSuccessMessage('Work resumed!');
      } catch (err) {
        setErrorMessage(err.message || 'Failed to resume work');
      }
    } else {
      syncEngine.enqueueAction({
        action: 'RESUME',
        workOrderId: Number(id),
        expectedCurrentStatus: 'ON_HOLD',
        payload: { remarks: 'Resumed work offline' },
      });
      setJob((prev) => ({ ...prev, status: 'IN_PROGRESS' }));
      setTimerRunning(true);
      setSuccessMessage('Offline: Work resumed locally.');
    }
    setActionLoading(false);
  };

  const handleCompleteWork = async () => {
    if (!completionNotes.trim()) {
      alert('Please provide completion notes describing work performed.');
      return;
    }
    if (!acknowledged || !acknowledgementName.trim()) {
      alert('Digital acknowledgement signature is required prior to completion submission.');
      return;
    }

    const evidenceMetadata = JSON.stringify({
      beforePhotos,
      afterPhotos,
      materialsUsed: materials.filter((m) => m.name.trim() !== ''),
      safetyChecklist,
      digitalSign: {
        acknowledgedBy: acknowledgementName,
        timestamp: new Date().toISOString(),
      },
      trackedDurationMinutes: Math.round(elapsedSeconds / 60),
    });

    const payload = {
      completionNotes,
      actualCost: Number(actualCost) || 0,
      evidenceMetadata,
    };

    setActionLoading(true);
    setErrorMessage('');

    if (syncEngine.isOnline()) {
      try {
        const updated = await workOrderService.completeJob(id, payload);
        setJob(updated);
        syncEngine.updateLocalJob(id, updated);
        syncEngine.clearDraft(id);
        setTimerRunning(false);
        setSuccessMessage('Work order completed and submitted for supervisor verification!');
      } catch (err) {
        setErrorMessage(err.message || 'Failed to complete work');
      }
    } else {
      syncEngine.enqueueAction({
        action: 'COMPLETE',
        workOrderId: Number(id),
        expectedCurrentStatus: 'IN_PROGRESS',
        payload,
      });
      setJob((prev) => ({
        ...prev,
        status: 'COMPLETED',
        actualCompletionDate: new Date().toISOString(),
        completionNotes,
        actualCost: Number(actualCost) || 0,
      }));
      syncEngine.clearDraft(id);
      setTimerRunning(false);
      setSuccessMessage('Offline: Work marked COMPLETED locally. Queued for server sync.');
    }
    setActionLoading(false);
  };

  const handleAddComment = async (e) => {
    e.preventDefault();
    if (!newComment.trim()) return;

    if (syncEngine.isOnline()) {
      try {
        const added = await workOrderService.addComment(id, newComment, true);
        setComments((prev) => [...prev, added]);
        setNewComment('');
      } catch (err) {
        alert('Failed to send comment: ' + err.message);
      }
    } else {
      syncEngine.enqueueAction({
        action: 'COMMENT',
        workOrderId: Number(id),
        payload: { comment: newComment, internalOnly: true },
      });
      setComments((prev) => [
        ...prev,
        {
          id: Date.now(),
          authorName: user?.fullName || 'Me (Offline)',
          authorRole: 'ROLE_FIELD_WORKER',
          comment: newComment,
          createdAt: new Date().toISOString(),
        },
      ]);
      setNewComment('');
    }
  };

  // Photo upload mock/local file reader
  const handlePhotoUpload = (e, type) => {
    const file = e.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      const photoItem = {
        name: file.name,
        dataUrl: event.target.result,
        timestamp: new Date().toISOString(),
      };
      if (type === 'before') {
        setBeforePhotos((prev) => [...prev, photoItem]);
      } else {
        setAfterPhotos((prev) => [...prev, photoItem]);
      }
    };
    reader.readAsDataURL(file);
  };

  // Format Elapsed Seconds
  const formatTime = (totalSeconds) => {
    const hrs = Math.floor(totalSeconds / 3600);
    const mins = Math.floor((totalSeconds % 3600) / 60);
    const secs = totalSeconds % 60;
    return `${hrs.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  if (loading) {
    return (
      <div className="p-12 text-center text-xs text-gray-500">
        <span className="inline-block animate-spin mr-2">⟳</span> Loading work order...
      </div>
    );
  }

  if (!job) {
    return (
      <div className="p-6 bg-white rounded-xl border border-red-200 text-center">
        <h2 className="text-sm font-bold text-red-600">Error Loading Job</h2>
        <p className="text-xs text-gray-500 mt-1">{errorMessage || 'Work order not found.'}</p>
        <button
          onClick={() => navigate('/operations')}
          className="mt-4 px-4 py-1.5 bg-gray-100 hover:bg-gray-200 text-xs font-semibold rounded-lg"
        >
          Back to Dashboard
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-6 pb-12">
      {/* Top Navigation */}
      <div className="flex items-center justify-between pb-2 border-b border-gray-200">
        <button
          onClick={() => navigate('/operations')}
          className="text-xs text-blue-600 hover:text-blue-800 font-medium flex items-center gap-1"
        >
          &larr; Back to Dashboard
        </button>
        <div className="text-xs font-mono font-bold text-gray-700">{job.workOrderNumber}</div>
      </div>

      <SyncStatusBanner onSyncComplete={loadJobData} />

      {errorMessage && (
        <div className="p-3 bg-red-50 border border-red-200 text-red-800 text-xs rounded-lg">{errorMessage}</div>
      )}
      {successMessage && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs rounded-lg">
          {successMessage}
        </div>
      )}

      {/* Primary Job Banner */}
      <div className="bg-white rounded-xl border border-gray-200 p-5 shadow-2xs space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-gray-100 pb-3">
          <div>
            <div className="flex items-center gap-2">
              <span className="text-xs font-mono font-bold text-blue-600">{job.workOrderNumber}</span>
              <span className="text-[11px] font-semibold px-2 py-0.5 rounded bg-blue-100 text-blue-800">
                {job.status}
              </span>
              <span className="text-[11px] font-semibold px-2 py-0.5 rounded bg-amber-100 text-amber-800">
                {job.priority}
              </span>
            </div>
            <h1 className="text-lg font-bold text-gray-900 mt-1">{job.title}</h1>
          </div>

          {/* Action Execution Buttons */}
          <div className="flex items-center gap-2 flex-wrap">
            {job.status === 'ASSIGNED' && (
              <button
                onClick={handleAcceptJob}
                disabled={actionLoading}
                className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold text-xs rounded-lg shadow-xs transition-colors"
              >
                Accept Assignment
              </button>
            )}

            {job.status === 'ACCEPTED' && (
              <button
                onClick={handleStartWork}
                disabled={actionLoading}
                className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-xs rounded-lg shadow-xs transition-colors"
              >
                Start On-Site Work
              </button>
            )}

            {job.status === 'IN_PROGRESS' && (
              <>
                <button
                  onClick={handlePauseWork}
                  disabled={actionLoading}
                  className="px-3 py-2 bg-amber-500 hover:bg-amber-600 text-white font-semibold text-xs rounded-lg shadow-xs transition-colors"
                >
                  Pause Work
                </button>
              </>
            )}

            {job.status === 'ON_HOLD' && (
              <button
                onClick={handleResumeWork}
                disabled={actionLoading}
                className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-xs rounded-lg shadow-xs transition-colors"
              >
                Resume Work
              </button>
            )}
          </div>
        </div>

        {/* Location & GPS Check-in */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
          <div className="space-y-1">
            <span className="font-semibold text-gray-500 uppercase tracking-wide text-[10px]">Site Location</span>
            <p className="text-gray-900 font-medium">{job.addressLine || 'Location details specified on site map'}</p>
            {job.wardName && <p className="text-gray-500">Ward: {job.wardName}</p>}
            {job.latitude && job.longitude && (
              <p className="text-gray-500 font-mono text-[11px]">
                Target GPS: {job.latitude}, {job.longitude}
              </p>
            )}
          </div>

          <div className="space-y-2 bg-slate-50 p-3 rounded-lg border border-slate-100">
            <div className="flex items-center justify-between">
              <span className="font-semibold text-gray-600 text-[11px]">GPS Verification</span>
              <button
                onClick={handleGpsCheckIn}
                className="px-2.5 py-1 bg-white border border-gray-300 hover:bg-gray-50 text-gray-800 text-[11px] font-semibold rounded shadow-2xs transition-colors"
              >
                📍 Stamp GPS Check-in
              </button>
            </div>
            {lastCheckin ? (
              <p className="text-[11px] text-emerald-700 font-medium">
                Verified at: {lastCheckin.latitude.toFixed(5)}, {lastCheckin.longitude.toFixed(5)} (
                {new Date(lastCheckin.timestamp).toLocaleTimeString()})
              </p>
            ) : (
              <p className="text-[11px] text-gray-400 italic">No GPS stamp recorded yet on arrival.</p>
            )}
          </div>
        </div>

        {/* Time Tracking Widget */}
        {(job.status === 'IN_PROGRESS' || timerRunning || elapsedSeconds > 0) && (
          <div className="bg-emerald-50 border border-emerald-200 p-3 rounded-lg flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="text-xs text-emerald-800 font-semibold uppercase tracking-wide">
                Live Time Tracking
              </div>
              <div className="font-mono text-lg font-bold text-emerald-950">{formatTime(elapsedSeconds)}</div>
            </div>
            <div className="text-[11px] text-emerald-700">
              {timerRunning ? 'Timer active' : 'Timer stopped/paused'}
            </div>
          </div>
        )}
      </div>

      {/* Safety Checklist Section */}
      <div className="bg-white rounded-xl border border-gray-200 p-5 shadow-2xs space-y-3">
        <h2 className="text-xs font-bold text-gray-900 uppercase tracking-wide">
          1. Mandatory Safety & Operational Checklist
        </h2>
        <p className="text-xs text-gray-500">
          Field crew must inspect and confirm site safety prior to commencing physical works.
        </p>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5 pt-1">
          {[
            { key: 'ppeEquipped', label: 'Safety PPE equipped (Helmets, boots, vests, gloves)' },
            { key: 'siteBarricaded', label: 'Perimeter barricaded with warning cones / tape' },
            { key: 'trafficDiverted', label: 'Traffic / pedestrian flow safely managed' },
            { key: 'powerIsolated', label: 'Nearby electrical lines / connections isolated' },
            { key: 'gasChecked', label: 'Underground utilities / water lines verified' },
          ].map((item) => (
            <label
              key={item.key}
              className="flex items-center gap-2.5 text-xs text-gray-700 cursor-pointer bg-slate-50 p-2.5 rounded-lg border border-slate-100 hover:bg-slate-100/70 transition-colors"
            >
              <input
                type="checkbox"
                checked={safetyChecklist[item.key]}
                onChange={(e) =>
                  setSafetyChecklist((prev) => ({ ...prev, [item.key]: e.target.checked }))
                }
                className="w-4 h-4 text-blue-600 rounded border-gray-300 focus:ring-blue-500"
              />
              <span className="font-medium">{item.label}</span>
            </label>
          ))}
        </div>
      </div>

      {/* Work Execution, Photos, and Materials Section */}
      {['IN_PROGRESS', 'ON_HOLD', 'COMPLETED', 'PENDING_VERIFICATION'].includes(job.status) && (
        <div className="bg-white rounded-xl border border-gray-200 p-5 shadow-2xs space-y-5">
          <h2 className="text-xs font-bold text-gray-900 uppercase tracking-wide">
            2. Site Evidence & Material Tracking
          </h2>

          {/* Photographs */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Before Photos */}
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-gray-700">Before Work Photographs</span>
                <label className="cursor-pointer text-[11px] bg-gray-100 hover:bg-gray-200 text-gray-800 px-2 py-1 rounded font-medium">
                  + Add Before Photo
                  <input
                    type="file"
                    accept="image/*"
                    capture="environment"
                    className="hidden"
                    onChange={(e) => handlePhotoUpload(e, 'before')}
                  />
                </label>
              </div>
              <div className="grid grid-cols-3 gap-2 min-h-20 bg-slate-50 p-2 rounded-lg border border-slate-100">
                {beforePhotos.map((p, idx) => (
                  <div key={idx} className="relative group rounded overflow-hidden aspect-video bg-gray-200">
                    <img src={p.dataUrl} alt="Before" className="w-full h-full object-cover" />
                  </div>
                ))}
                {beforePhotos.length === 0 && (
                  <div className="col-span-3 text-center text-[11px] text-gray-400 self-center">
                    No initial photos uploaded
                  </div>
                )}
              </div>
            </div>

            {/* After Photos */}
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-gray-700">After Work Photographs</span>
                <label className="cursor-pointer text-[11px] bg-gray-100 hover:bg-gray-200 text-gray-800 px-2 py-1 rounded font-medium">
                  + Add After Photo
                  <input
                    type="file"
                    accept="image/*"
                    capture="environment"
                    className="hidden"
                    onChange={(e) => handlePhotoUpload(e, 'after')}
                  />
                </label>
              </div>
              <div className="grid grid-cols-3 gap-2 min-h-20 bg-slate-50 p-2 rounded-lg border border-slate-100">
                {afterPhotos.map((p, idx) => (
                  <div key={idx} className="relative group rounded overflow-hidden aspect-video bg-gray-200">
                    <img src={p.dataUrl} alt="After" className="w-full h-full object-cover" />
                  </div>
                ))}
                {afterPhotos.length === 0 && (
                  <div className="col-span-3 text-center text-[11px] text-gray-400 self-center">
                    No completion photos uploaded
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Materials Usage Table */}
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-gray-700">Consumables & Materials Consumed</span>
              <button
                type="button"
                onClick={() => setMaterials((prev) => [...prev, { name: '', quantity: '', unit: 'units' }])}
                className="text-[11px] bg-gray-100 hover:bg-gray-200 text-gray-800 px-2 py-1 rounded font-medium"
              >
                + Add Material
              </button>
            </div>
            <div className="space-y-2">
              {materials.map((mat, idx) => (
                <div key={idx} className="flex gap-2">
                  <input
                    type="text"
                    placeholder="Material name (e.g. Cold Mix Bitumen, Cement bag)"
                    value={mat.name}
                    onChange={(e) => {
                      const val = e.target.value;
                      setMaterials((prev) => prev.map((m, i) => (i === idx ? { ...m, name: val } : m)));
                    }}
                    className="flex-1 text-xs border border-gray-300 rounded-lg px-3 py-1.5 focus:ring-1 focus:ring-blue-500"
                  />
                  <input
                    type="number"
                    placeholder="Qty"
                    value={mat.quantity}
                    onChange={(e) => {
                      const val = e.target.value;
                      setMaterials((prev) => prev.map((m, i) => (i === idx ? { ...m, quantity: val } : m)));
                    }}
                    className="w-20 text-xs border border-gray-300 rounded-lg px-2 py-1.5 focus:ring-1 focus:ring-blue-500"
                  />
                  <input
                    type="text"
                    placeholder="Unit (kg, bags)"
                    value={mat.unit}
                    onChange={(e) => {
                      const val = e.target.value;
                      setMaterials((prev) => prev.map((m, i) => (i === idx ? { ...m, unit: val } : m)));
                    }}
                    className="w-24 text-xs border border-gray-300 rounded-lg px-2 py-1.5 focus:ring-1 focus:ring-blue-500"
                  />
                </div>
              ))}
            </div>
          </div>

          {/* Completion Notes and Actual Cost */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 pt-2">
            <div className="sm:col-span-2 space-y-1">
              <label className="text-xs font-semibold text-gray-700">Detailed Work Notes</label>
              <textarea
                rows={3}
                placeholder="Describe actions taken, site conditions, challenges encountered, or quality details..."
                value={completionNotes}
                onChange={(e) => setCompletionNotes(e.target.value)}
                className="w-full text-xs border border-gray-300 rounded-lg p-2.5 focus:ring-1 focus:ring-blue-500"
              />
            </div>
            <div className="space-y-1">
              <label className="text-xs font-semibold text-gray-700">Actual Cost Incurred (₹)</label>
              <input
                type="number"
                placeholder="0.00"
                value={actualCost}
                onChange={(e) => setActualCost(e.target.value)}
                className="w-full text-xs border border-gray-300 rounded-lg p-2.5 focus:ring-1 focus:ring-blue-500"
              />
            </div>
          </div>

          {/* Digital Acknowledgement */}
          <div className="bg-slate-50 p-4 rounded-xl border border-slate-200 space-y-3">
            <h3 className="text-xs font-bold text-gray-900 uppercase tracking-wide">
              3. Digital Crew Acknowledgement
            </h3>
            <div className="flex flex-col sm:flex-row gap-3 items-start sm:items-center">
              <input
                type="text"
                placeholder="Lead Field Worker Full Name"
                value={acknowledgementName}
                onChange={(e) => setAcknowledgementName(e.target.value)}
                className="text-xs border border-gray-300 rounded-lg px-3 py-2 w-full sm:w-64 bg-white"
              />
              <label className="flex items-center gap-2 text-xs text-gray-700 cursor-pointer">
                <input
                  type="checkbox"
                  checked={acknowledged}
                  onChange={(e) => setAcknowledged(e.target.checked)}
                  className="w-4 h-4 text-blue-600 rounded border-gray-300"
                />
                <span className="font-medium">
                  I certify that the repair work has been performed to municipal standards and site is left clean.
                </span>
              </label>
            </div>
          </div>

          {/* Submit Completion Button */}
          {job.status === 'IN_PROGRESS' && (
            <div className="pt-2 flex justify-end">
              <button
                type="button"
                onClick={handleCompleteWork}
                disabled={actionLoading}
                className="px-6 py-2.5 bg-purple-600 hover:bg-purple-700 text-white font-bold text-xs rounded-lg shadow-sm transition-colors"
              >
                Complete Job & Submit for Verification
              </button>
            </div>
          )}
        </div>
      )}

      {/* Crew Communication / Comments Feed */}
      <div className="bg-white rounded-xl border border-gray-200 p-5 shadow-2xs space-y-4">
        <h2 className="text-xs font-bold text-gray-900 uppercase tracking-wide">Crew Log & Operational Notes</h2>

        <div className="space-y-3 max-h-60 overflow-y-auto pr-2">
          {comments.map((comm) => (
            <div key={comm.id} className="bg-slate-50 p-3 rounded-lg border border-slate-100 text-xs space-y-1">
              <div className="flex justify-between items-center text-gray-500 text-[11px]">
                <span className="font-semibold text-gray-800">{comm.authorName}</span>
                <span>{new Date(comm.createdAt).toLocaleString()}</span>
              </div>
              <p className="text-gray-700">{comm.comment}</p>
            </div>
          ))}
          {comments.length === 0 && (
            <p className="text-xs text-gray-400 italic">No notes recorded yet.</p>
          )}
        </div>

        <form onSubmit={handleAddComment} className="flex gap-2">
          <input
            type="text"
            placeholder="Write internal crew note or status update..."
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
            className="flex-1 text-xs border border-gray-300 rounded-lg px-3 py-2 focus:ring-1 focus:ring-blue-500"
          />
          <button
            type="submit"
            className="px-4 py-2 bg-slate-800 hover:bg-slate-900 text-white font-semibold text-xs rounded-lg shadow-2xs transition-colors"
          >
            Post Note
          </button>
        </form>
      </div>
    </div>
  );
};

export default JobExecutionPage;
