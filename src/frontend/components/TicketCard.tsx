'use client';

import Link from 'next/link';
import { ReactNode } from 'react';

interface Ticket {
  id: number;
  title: string;
  status: string;
  priority: string;
  createdAt: string;
}

interface TicketCardProps {
  ticket: Ticket;
}

const statusColors: Record<string, string> = {
  OPEN: 'bg-blue-100 text-blue-800',
  IN_PROGRESS: 'bg-yellow-100 text-yellow-800',
  RESOLVED: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-gray-100 text-gray-800',
  CLOSED: 'bg-gray-200 text-gray-600',
};

const priorityColors: Record<string, string> = {
  LOW: 'bg-gray-100 text-gray-800',
  MEDIUM: 'bg-blue-100 text-blue-800',
  HIGH: 'bg-orange-100 text-orange-800',
  URGENT: 'bg-red-100 text-red-800',
};

export function TicketCard({ ticket }: TicketCardProps) {
  const statusColor = statusColors[ticket.status] || 'bg-gray-100 text-gray-800';
  const priorityColor = priorityColors[ticket.priority] || 'bg-gray-100 text-gray-800';

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow">
      <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between mb-4">
        <h3 className="text-lg font-semibold text-gray-900 mb-2 sm:mb-0">
          <Link href={`/tickets/${ticket.id}`} className="hover:text-blue-600">
            {ticket.title}
          </Link>
        </h3>
        <div className="flex flex-wrap gap-2">
          <span className={`px-2.5 py-0.5 rounded-full text-xs font-medium ${statusColor}`}>
            {ticket.status}
          </span>
          <span className={`px-2.5 py-0.5 rounded-full text-xs font-medium ${priorityColor}`}>
            {ticket.priority}
          </span>
        </div>
      </div>

      <div className="text-sm text-gray-500 mt-4">
        Created: {formatDate(ticket.createdAt)}
      </div>
    </div>
  );
}
