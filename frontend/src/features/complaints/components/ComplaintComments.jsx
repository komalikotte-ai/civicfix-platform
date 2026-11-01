import React, { useState } from 'react';
import Button from '../../../components/common/Button';
import Textarea from '../../../components/common/Textarea';
import complaintService from '../../../services/complaintService';

export const ComplaintComments = ({ complaintId, comments = [], onCommentAdded }) => {
  const [newComment, setNewComment] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!newComment.trim()) return;

    if (newComment.trim().length < 5) {
      setError('Comment must be at least 5 characters long.');
      return;
    }

    setSubmitting(true);
    setError('');
    try {
      await complaintService.addComment(complaintId, {
        comment: newComment.trim(),
        internalOnly: false,
      });
      setNewComment('');
      if (onCommentAdded) {
        onCommentAdded();
      }
    } catch (err) {
      setError(err.message || 'Failed to post comment.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="flex flex-col gap-4">
      {/* Comment submission form */}
      <form onSubmit={handleSubmit} className="flex flex-col gap-2">
        <label className="text-xs font-semibold text-slate-700">
          Add an Update or Inquiry
        </label>
        <Textarea
          value={newComment}
          onChange={(e) => setNewComment(e.target.value)}
          placeholder="Provide additional details, landmark instructions, or ask for an update..."
          rows={3}
          maxLength={1000}
          showCount
        />
        {error && (
          <div className="text-xs text-red-600 font-medium">{error}</div>
        )}
        <div className="flex justify-end">
          <Button
            type="submit"
            size="sm"
            variant="primary"
            loading={submitting}
            disabled={!newComment.trim()}
          >
            Post Comment
          </Button>
        </div>
      </form>

      {/* Comments List */}
      <div className="border-t border-slate-100 pt-3">
        <h4 className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-3">
          Discussion & Resolution Log ({comments.length})
        </h4>

        {comments.length === 0 ? (
          <div className="py-6 text-center text-xs text-slate-500 bg-slate-50 rounded-xl border border-dashed border-slate-200">
            No public comments or citizen queries on this grievance yet.
          </div>
        ) : (
          <div className="space-y-3">
            {comments.map((item, idx) => {
              const isFeedback = item.comment?.includes('[CITIZEN_FEEDBACK]');
              return (
                <div
                  key={item.id || idx}
                  className={`p-3.5 rounded-xl border text-xs ${
                    isFeedback
                      ? 'bg-amber-50/60 border-amber-200 shadow-xs'
                      : 'bg-white border-slate-200'
                  }`}
                >
                  <div className="flex items-center justify-between mb-1.5">
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-slate-900">
                        {item.authorName || 'Citizen User'}
                      </span>
                      <span
                        className={`px-1.5 py-0.5 rounded text-[10px] font-semibold ${
                          isFeedback
                            ? 'bg-amber-200 text-amber-900'
                            : item.authorRole === 'CITIZEN'
                            ? 'bg-blue-100 text-blue-800'
                            : 'bg-purple-100 text-purple-800'
                        }`}
                      >
                        {isFeedback ? 'Citizen Feedback' : item.authorRole || 'CITIZEN'}
                      </span>
                    </div>
                    <time className="text-[11px] font-mono text-slate-400">
                      {item.createdAt ? new Date(item.createdAt).toLocaleString() : 'Just now'}
                    </time>
                  </div>
                  <p className="text-slate-800 leading-relaxed whitespace-pre-wrap">
                    {item.comment}
                  </p>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};

export default ComplaintComments;
