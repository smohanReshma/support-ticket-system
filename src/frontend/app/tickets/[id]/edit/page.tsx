'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { apiClient } from '../../../../lib/api/client';
import { ValidationErrorBanner } from '../../../../components/ValidationErrorBanner';

type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

interface FormErrors {
  title?: string;
  description?: string;
  priority?: string;
  assignee?: string;
}

export default function EditTicketPage({ params }: { params: { id: string } }) {
  const router = useRouter();
  const id = parseInt(params.id, 10);
  const [loading, setLoading] = useState(false);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState<Priority>('HIGH');
  const [assignee, setAssignee] = useState('');
  const [violations, setViolations] = useState<any[]>([]);
  const [apiError, setApiError] = useState<string | null>(null);

  const fetchData = async () => {
    setLoading(true);

    try {
      const ticket = await apiClient.getTicket(id);
      setTitle(ticket.title);
      setDescription(ticket.description);
      setPriority(ticket.priority as Priority);
      setAssignee(ticket.assignee || '');
    } catch (err: any) {
      setApiError(err.message || 'Failed to load ticket');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [id]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setViolations([]);
    setApiError(null);

    try {
      await apiClient.updateTicket(id, {
        title: title || undefined,
        description: description || undefined,
        priority: priority as string,
        assignee: assignee || undefined,
      });
      router.push(`/tickets/${id}`);
    } catch (err: any) {
      if (err.status === 400 && err.details?.violations) {
        setViolations(err.details.violations);
      } else {
        setApiError(err.message || 'Failed to update ticket');
      }
    } finally {
      setLoading(false);
    }
  };

  if (loading && !title) {
    return (
      <div className="flex justify-center items-center py-12">
        <div className="text-gray-500">Loading ticket...</div>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Edit Ticket</h1>

      {apiError && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-4">
          <h3 className="text-red-800 font-medium">Error</h3>
          <p className="text-red-600 mt-1">{apiError}</p>
        </div>
      )}

      {violations.length > 0 && (
        <ValidationErrorBanner violations={violations} />
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label htmlFor="title" className="block text-sm font-medium text-gray-700">
            Title <span className="text-red-600">*</span>
          </label>
          <input
            type="text"
            id="title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            required
            maxLength={255}
          />
        </div>

        <div>
          <label htmlFor="description" className="block text-sm font-medium text-gray-700">
            Description <span className="text-red-600">*</span>
          </label>
          <textarea
            id="description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={5}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            required
            maxLength={10000}
          />
        </div>

        <div>
          <label htmlFor="priority" className="block text-sm font-medium text-gray-700">
            Priority <span className="text-red-600">*</span>
          </label>
          <select
            id="priority"
            value={priority}
            onChange={(e) => setPriority(e.target.value as Priority)}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="LOW">LOW - Low priority issue</option>
            <option value="MEDIUM">MEDIUM - Medium priority issue</option>
            <option value="HIGH">HIGH - High priority issue</option>
            <option value="URGENT">URGENT - Urgent priority issue</option>
          </select>
        </div>

        <div>
          <label htmlFor="assignee" className="block text-sm font-medium text-gray-700">
            Assignee
          </label>
          <input
            type="text"
            id="assignee"
            value={assignee}
            onChange={(e) => setAssignee(e.target.value)}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            placeholder="Optional: john.doe@example.com"
            maxLength={255}
          />
        </div>

        <div className="flex gap-3 pt-4">
          <button
            type="submit"
            disabled={loading}
            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? 'Updating...' : 'Update Ticket'}
          </button>
          <button
            type="button"
            onClick={() => router.push(`/tickets/${id}`)}
            className="px-4 py-2 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}
