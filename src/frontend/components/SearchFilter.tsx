'use client';

import { useState, useEffect, ReactNode } from 'react';

interface SearchFilterProps {
  initialSearch?: string;
  initialStatus?: string;
  onSearch?: (search: string) => void;
  onStatus?: (status: string) => void;
  onReset?: () => void;
}

export function SearchFilter({
  initialSearch = '',
  initialStatus = '',
  onSearch,
  onStatus,
  onReset,
}: SearchFilterProps) {
  const [searchTerm, setSearchTerm] = useState(initialSearch);
  const [status, setStatus] = useState(initialStatus);

  const MAX_SEARCH_LENGTH = 100;
  const [searchError, setSearchError] = useState('');

  const statusOptions = [
    { value: '', label: 'All Statuses' },
    { value: 'OPEN', label: 'Open' },
    { value: 'IN_PROGRESS', label: 'In Progress' },
    { value: 'RESOLVED', label: 'Resolved' },
    { value: 'CANCELLED', label: 'Cancelled' },
    { value: 'CLOSED', label: 'Closed' },
  ];

  useEffect(() => {
    if (onSearch) {
      onSearch(searchTerm);
    }
  }, [searchTerm, onSearch]);

  useEffect(() => {
    if (onStatus) {
      onStatus(status);
    }
  }, [status, onStatus]);

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    if (value.length > MAX_SEARCH_LENGTH) {
      setSearchError(`Search must be ${MAX_SEARCH_LENGTH} characters or less`);
      return;
    }
    setSearchError('');
    setSearchTerm(value);
  };

  const handleStatusChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setStatus(e.target.value);
  };

  const handleReset = () => {
    setSearchTerm('');
    setStatus('');
    setSearchError('');
    if (onReset) {
      onReset();
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 mb-6">
      <div className="flex flex-col sm:flex-row gap-4">
        <div className="flex-1">
          <label htmlFor="search" className="block text-sm font-medium text-gray-700 mb-1">
            Search
          </label>
          <input
            type="text"
            id="search"
            value={searchTerm}
            onChange={handleSearchChange}
            placeholder="Search by title or description..."
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
          {searchError && (
            <p className="mt-1 text-sm text-red-600">{searchError}</p>
          )}
        </div>

        <div className="sm:w-48">
          <label htmlFor="status" className="block text-sm font-medium text-gray-700 mb-1">
            Status
          </label>
          <select
            id="status"
            value={status}
            onChange={handleStatusChange}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            {statusOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        <div className="flex items-end">
          <button
            onClick={handleReset}
            className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            Reset
          </button>
        </div>
      </div>
    </div>
  );
}
