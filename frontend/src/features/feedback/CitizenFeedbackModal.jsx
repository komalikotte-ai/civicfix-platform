import React, { useState } from 'react';
import Modal from '../../components/common/Modal';
import Button from '../../components/common/Button';
import Textarea from '../../components/common/Textarea';
import Alert from '../../components/common/Alert';
import { feedbackService } from '../../services/feedbackService';

export const CitizenFeedbackModal = ({
  isOpen,
  complaint,
  onClose,
  onSuccess,
}) => {
  const [rating, setRating] = useState(5);
  const [satisfied, setSatisfied] = useState(true);
  const [feedbackComments, setFeedbackComments] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  if (!complaint) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);

    try {
      await feedbackService.submitFeedback(complaint.id, {
        rating,
        resolutionSatisfied: satisfied,
        feedbackComments,
      });
      if (onSuccess) onSuccess();
    } catch (err) {
      setError(err.message || 'Failed to submit feedback. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Citizen Satisfaction Feedback"
      description={`Share your feedback on the resolution of grievance #${complaint.trackingNumber || complaint.id}`}
      footer={
        <>
          <Button variant="ghost" onClick={onClose} disabled={submitting}>
            Cancel
          </Button>
          <Button
            variant="primary"
            onClick={handleSubmit}
            loading={submitting}
          >
            Submit Feedback
          </Button>
        </>
      }
    >
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        {error && <Alert type="error" message={error} />}

        <div className="bg-slate-50 p-3 rounded-lg border border-slate-200 text-xs">
          <div className="font-semibold text-slate-800">{complaint.title}</div>
          <div className="text-slate-500 mt-0.5">
            Category: {complaint.category || 'General'} | Status: {complaint.status}
          </div>
        </div>

        {/* Star Rating */}
        <div className="flex flex-col gap-1.5">
          <label className="text-xs font-semibold text-slate-700">
            How satisfied are you with the resolution? (1 to 5 Stars)
          </label>
          <div className="flex items-center gap-2" role="radiogroup" aria-label="Rating">
            {[1, 2, 3, 4, 5].map((star) => (
              <button
                key={star}
                type="button"
                role="radio"
                aria-checked={rating === star}
                aria-label={`${star} star${star > 1 ? 's' : ''}`}
                onClick={() => setRating(star)}
                className={`text-2xl p-1 rounded transition-transform hover:scale-110 focus:outline-none focus:ring-2 focus:ring-blue-400 ${
                  star <= rating ? 'text-amber-400' : 'text-slate-200'
                }`}
              >
                ★
              </button>
            ))}
            <span className="text-xs font-semibold text-slate-600 ml-2">
              {rating === 5 && 'Excellent'}
              {rating === 4 && 'Good'}
              {rating === 3 && 'Average'}
              {rating === 2 && 'Poor'}
              {rating === 1 && 'Very Poor'}
            </span>
          </div>
        </div>

        {/* Resolved Satisfactorily */}
        <div className="flex flex-col gap-1.5">
          <label className="text-xs font-semibold text-slate-700">
            Was the civic issue resolved properly on-ground?
          </label>
          <div className="flex items-center gap-4 text-xs font-medium text-slate-700">
            <label className="inline-flex items-center gap-1.5 cursor-pointer">
              <input
                type="radio"
                name="satisfied"
                checked={satisfied === true}
                onChange={() => setSatisfied(true)}
                className="text-blue-600 focus:ring-blue-500"
              />
              <span>Yes, completely resolved</span>
            </label>
            <label className="inline-flex items-center gap-1.5 cursor-pointer">
              <input
                type="radio"
                name="satisfied"
                checked={satisfied === false}
                onChange={() => setSatisfied(false)}
                className="text-blue-600 focus:ring-blue-500"
              />
              <span>No, still needs attention</span>
            </label>
          </div>
        </div>

        {/* Feedback Comments */}
        <Textarea
          label="Additional Comments (Optional)"
          placeholder="Let us know how the field team performed or if any issues remain..."
          value={feedbackComments}
          onChange={(e) => setFeedbackComments(e.target.value)}
          rows={3}
          maxLength={500}
        />
      </form>
    </Modal>
  );
};

export default CitizenFeedbackModal;
