import apiClient from './apiClient.js';
import { complaintService } from './complaintService.js';

export const feedbackService = {
  submitFeedback: async (complaintId, { rating, feedbackComments, resolutionSatisfied }) => {
    const formattedComment = `[CITIZEN_FEEDBACK] Rating: ${rating}/5 | Satisfied: ${resolutionSatisfied ? 'Yes' : 'No'} | Comments: ${feedbackComments || 'None'}`;
    return complaintService.addComment(complaintId, {
      comment: formattedComment,
      internalOnly: false,
    });
  },

  getFeedbackForComplaint: async (complaintId) => {
    const comments = await complaintService.getComments(complaintId);
    const feedbackList = (Array.isArray(comments) ? comments : []).filter((c) =>
      c.comment?.includes('[CITIZEN_FEEDBACK]')
    );
    return feedbackList;
  },
};

export default feedbackService;
