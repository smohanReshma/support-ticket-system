/**
 * API endpoint constants.
 */
export const API_BASE_URL = process.env.NEXT_PUBLIC_BACKEND_URL || 'http://localhost:8080';

export const endpoints = {
  tickets: () => '/tickets',
  ticket: (id: number | string) => `/tickets/${id}`,
  comments: (ticketId: number | string) => `/tickets/${ticketId}/comments`,
  comment: (ticketId: number | string, commentId: number | string) =>
    `/tickets/${ticketId}/comments/${commentId}`,
  transition: (ticketId: number | string) => `/tickets/${ticketId}/transition`,
};
