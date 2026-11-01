import React, { useState } from 'react';
import Modal from '../../../components/common/Modal';
import Button from '../../../components/common/Button';
import Textarea from '../../../components/common/Textarea';
import Alert from '../../../components/common/Alert';
import complaintService from '../../../services/complaintService';

export const ReopenComplaintModal = ({ isOpen, complaint, onClose, onSuccess }) => {
  const [reason, setReason] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (!complaint) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!reason.trim()) {
      setError('Please provide a reason why this complaint is being reopened.');
      return;
    }

    if (reason.trim().length < 10) {
      setError('Reason must be at least 10 characters long with specific details.');
      return;
    }

    setLoading(true);
    setError('');
    try {
      await complaintService.reopenComplaint(complaint.id, reason.trim());
      setReason('');
      if (onSuccess) {
        onSuccess();
      }
    } catch (err) {
      setError(err.message || 'Failed to reopen complaint. Please check your permissions.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={`Reopen Grievance #${complaint.trackingNumber || complaint.id}`}
      size="md"
    >
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <div className="p-3.5 bg-amber-50 border border-amber-200 rounded-xl text-xs text-amber-900 leading-relaxed">
          <span className="font-bold block mb-1">⚠️ Municipal Grievance Reopening Policy:</span>
          If work was incomplete, substandard, or the civic issue reoccurred, specify the ground reality below. Your grievance will be escalated back to department management for reinspection.
        </div>

        {error && <Alert type="error" message={error} onClose={() => setError('')} />}

        <div className="flex flex-col gap-1">
          <label className="text-xs font-semibold text-slate-700">
            Reopening Justification <span className="text-red-500">*</span>
          </label>
          <Textarea
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="e.g. The pothole was only filled with loose stones which were washed out after rain. Road surface is still uneven..."
            rows={4}
            maxLength={1000}
            showCount
            required
          />
        </div>

        <div className="flex justify-end gap-2.5 pt-2 border-t border-slate-100">
          <Button type="button" variant="ghost" onClick={onClose} disabled={loading}>
            Cancel
          </Button>
          <Button type="submit" variant="danger" loading={loading} disabled={!reason.trim()}>
            Confirm & Reopen Grievance
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default ReopenComplaintModal;
