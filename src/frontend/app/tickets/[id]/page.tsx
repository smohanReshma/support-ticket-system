'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { apiClient } from '../../../lib/api/client';
import { Ticket, Comment, ErrorResponse } from '../../../types/ticket';
import { TicketCard } from '../../../components/TicketCard';
import { TransitionErrorBanner } from '../../../components/TransitionErrorBanner';
import { ValidationErrorBanner } from '../../../components/ValidationErrorBanner';

export default function TicketDetailPage({ params }: { params: { id: string } }) {
  const router = useRouter();
  const id = parseInt(params.id, 10);
  const [ticket, setTicket] = useState<Ticket | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<ErrorResponse | null>(null);
  const [transitionError, setTransitionError] = useState<any>(null);

  const [commentText, setCommentText] = useState('');
  const [commentAuthor, setCommentAuthor] = useState('');

  const fetchData = async () => {
    setLoading(true);
    setError(null);

    try {
      const ticketData = await apiClient.getTicket(id);
      setTicket(ticketData);

      const commentsData = await apiClient.getComments(id);
      setComments(commentsData);
    } catch (err: any) {
      if (err.status === 404) {
        setError(err.details || { timestamp: '', status: 404, error: 'Not Found', message: 'Ticket not found', path: '' });
      } else {
        setError({ timestamp: '', status: err.status || 500, error: 'Internal Server Error', message: err.message || 'Failed to load ticket', path: '' });
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [id]);

  const handleAddComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!commentText.trim() || !commentAuthor.trim()) return;

    try {
      await apiClient.addComment(id, {
        text: commentText,
        author: commentAuthor,
      });
      setCommentText('');
      setCommentAuthor('');
      // Refresh comments
      const commentsData = await apiClient.getComments(id);
      setComments(commentsData);
    } catch (err: any) {
      setError(err.details || { timestamp: '', status: err.status || 500, error: 'Internal Server Error', message: err.message || 'Failed to add comment', path: '' });
    }
  };

  const handleTransition = async (targetStatus: string) => {
    try {
      const response = await apiClient.transitionTicket(id, { targetStatus });
      setTicket(response);
      setTransitionError(null);
      // Refresh comments
      const commentsData = await apiClient.getComments(id);
      setComments(commentsData);
    } catch (err: any) {
      if (err.status === 409 && err.details) {
        setTransitionError(err.details);
      } else {
        setError(err.details || { timestamp: '', status: err.status || 500, error: 'Internal Server Error', message: err.message || 'Failed to transition', path: '' });
      }
    }
  };

  const getValidTransitions = () => {
    if (!ticket) return [];
    const current = ticket.status;
    if (current === 'OPEN') return ['IN_PROGRESS', 'CANCELLED'];
    if (current === 'IN_PROGRESS') return ['RESOLVED', 'CANCELLED'];
    if (current === 'RESOLVED') return ['CLOSED'];
    return [];
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center py-12">
        <div className="text-gray-500">Loading ticket...</div>
      </div>
    );
  }

  if (error && !ticket) {
    return (
      <div className="max-w-2xl mx-auto">
        <h1 className="text-2xl font-bold text-gray-900 mb-4">Ticket Details</h1>
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <h3 className="text-red-800 font-medium">Error</h3>
          <p className="text-red-600 mt-1">{error.message}</p>
          <button
            onClick={fetchData}
            className="mt-4 px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  if (!ticket) return null;

  return (
    <div>
      <div className="mb-6">
        <button
          onClick={() => router.push('/')}
          className="text-gray-600 hover:text-gray-900 mb-4"
        >
          ← Back to list
        </button>
        <h1 className="text-2xl font-bold text-gray-900">Ticket Details</h1>
      </div>

      <TicketCard ticket={ticket} />

      <div className="mt-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Comments</h2>

        {comments.length === 0 ? (
          <p className="text-gray-500">No comments yet.</p>
        ) : (
          <div className="space-y-4">
            {comments.map((comment) => (
              <div
                key={comment.id}
                className="bg-white rounded-lg shadow-sm border border-gray-200 p-4"
              >
                <div className="flex justify-between items-start mb-2">
                  <span className="font-medium text-gray-900">{comment.author}</span>
                  <span className="text-sm text-gray-500">
                    {new Date(comment.createdAt).toLocaleString()}
                  </span>
                </div>
                <p className="text-gray-700">{comment.text}</p>
              </div>
            ))}
          </div>
        )}

        <div className="mt-6">
          <h3 className="text-md font-medium text-gray-900 mb-2">Add Comment</h3>
          {transitionError && (
            <TransitionErrorBanner
              error={{
                currentStatus: transitionError.currentStatus,
                requestedStatus: transitionError.requestedStatus,
                validTransitions: transitionError.validTransitions,
              }}
              onDismiss={() => setTransitionError(null)}
            />
          )}
          {error && (
            <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-4">
              <h3 className="text-red-800 font-medium">Error</h3>
              <p className="text-red-600 mt-1">{error.message}</p>
            </div>
          )}
          <form onSubmit={handleAddComment} className="space-y-3">
            <div>
              <label htmlFor="commentText" className="block text-sm font-medium text-gray-700">
                Comment <span className="text-red-600">*</span>
              </label>
              <textarea
                id="commentText"
                value={commentText}
                onChange={(e) => setCommentText(e.target.value)}
                rows={3}
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                required
              />
            </div>
            <div>
              <label htmlFor="commentAuthor" className="block text-sm font-medium text-gray-700">
                Your Name <span className="text-red-600">*</span>
              </label>
              <input
                type="text"
                id="commentAuthor"
                value={commentAuthor}
                onChange={(e) => setCommentAuthor(e.target.value)}
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                required
              />
            </div>
            <button
              type="submit"
              disabled={!commentText.trim() || !commentAuthor.trim() || loading}
              className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50"
            >
              Add Comment
            </button>
          </form>
        </div>
      </div>

      <div className="mt-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">State Transition</h2>
        {getValidTransitions().length > 0 ? (
          <div className="flex items-center gap-3">
            <select
              onChange={(e) => handleTransition(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              defaultValue=""
            >
              <option value="" disabled>
                Select transition...
              </option>
              {getValidTransitions().map((status) => (
                <option key={status} value={status}>
                  {status}
                </option>
              ))}
            </select>
            <span className="text-sm text-gray-500">Current: {ticket.status}</span>
          </div>
        ) : (
          <p className="text-gray-500">No transitions available for this ticket.</p>
        )}
      </div>
    </div>
  );
}
