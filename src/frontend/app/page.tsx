'use client';

import { useState, useEffect } from 'react';
import { apiClient } from '../lib/api/client';
import { TicketCard } from '../components/TicketCard';
import { SearchFilter } from '../components/SearchFilter';

interface Ticket {
  id: number;
  title: string;
  status: string;
  priority: string;
  createdAt: string;
}

interface TicketListResponse {
  content: Ticket[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export default function TicketListPage() {
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(0);
  const [size] = useState(20);

  const [listResponse, setListResponse] = useState<TicketListResponse | null>(null);

  const fetchData = async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await apiClient.getTickets({
        page,
        size,
        search: search || undefined,
        status: status || undefined,
      });

      setListResponse(response);
      setTickets(response.content || []);
    } catch (err: any) {
      setError(err.message || 'Failed to fetch tickets');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [page, search, status]);

  const handleResetSearch = () => {
    setPage(0);
    setSearch('');
    setStatus('');
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center py-12">
        <div className="text-gray-500">Loading tickets...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-lg p-4">
        <h3 className="text-red-800 font-medium">Error</h3>
        <p className="text-red-600 mt-1">{error}</p>
        <button
          onClick={fetchData}
          className="mt-4 px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700"
        >
          Retry
        </button>
      </div>
    );
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Tickets</h1>
        <span className="text-sm text-gray-500">
          {listResponse?.totalElements} tickets found
        </span>
      </div>

      <SearchFilter
        initialSearch={search}
        initialStatus={status}
        onSearch={setSearch}
        onStatus={setStatus}
        onReset={handleResetSearch}
      />

      {tickets.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-lg border border-gray-200">
          <p className="text-gray-500">No tickets found.</p>
        </div>
      ) : (
        <>
          <div className="grid gap-4">
            {tickets.map((ticket) => (
              <TicketCard key={ticket.id} ticket={ticket} />
            ))}
          </div>

          {listResponse && listResponse.totalPages > 1 && (
            <div className="flex justify-center mt-8 space-x-2">
              <button
                onClick={() => setPage(Math.max(0, page - 1))}
                disabled={page === 0}
                className="px-4 py-2 border border-gray-300 rounded-md bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Previous
              </button>
              <span className="px-4 py-2 text-gray-700">
                Page {page + 1} of {listResponse.totalPages}
              </span>
              <button
                onClick={() => setPage(Math.min(listResponse.totalPages - 1, page + 1))}
                disabled={page >= listResponse.totalPages - 1}
                className="px-4 py-2 border border-gray-300 rounded-md bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
