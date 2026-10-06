'use client';

import { useState } from 'react';

interface CommentFormProps {
  onSubmit: (text: string, author: string) => Promise<void>;
  loading: boolean;
}

export function CommentForm({ onSubmit, loading }: CommentFormProps) {
  const [text, setText] = useState('');
  const [author, setAuthor] = useState('');
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!text.trim() || !author.trim()) {
      setError('Both text and author are required');
      return;
    }

    try {
      await onSubmit(text, author);
      setText('');
      setAuthor('');
    } catch (err) {
      setError('Failed to add comment');
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-3">
      <div>
        <label htmlFor="commentText" className="block text-sm font-medium text-gray-700">
          Comment
        </label>
        <textarea
          id="commentText"
          value={text}
          onChange={(e) => setText(e.target.value)}
          rows={3}
          className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          required
        />
      </div>

      <div>
        <label htmlFor="commentAuthor" className="block text-sm font-medium text-gray-700">
          Your Name
        </label>
        <input
          type="text"
          id="commentAuthor"
          value={author}
          onChange={(e) => setAuthor(e.target.value)}
          className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          required
        />
      </div>

      {error && (
        <div className="text-red-600 text-sm">{error}</div>
      )}

      <button
        type="submit"
        disabled={!text.trim() || !author.trim() || loading}
        className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50"
      >
        {loading ? 'Adding...' : 'Add Comment'}
      </button>
    </form>
  );
}
