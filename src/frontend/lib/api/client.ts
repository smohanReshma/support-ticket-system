import { API_BASE_URL, endpoints } from './endpoints';

interface ApiError extends Error {
  status?: number;
  details?: any;
}

interface PaginationParams {
  page?: number;
  size?: number;
  search?: string;
  status?: string;
}

/**
 * Parse the response and throw an error for non-2xx status codes.
 */
async function parseResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const error: ApiError = new Error('API Error');
    error.status = response.status;

    try {
      error.details = await response.json();
    } catch {
      error.details = { message: await response.text() };
    }

    throw error;
  }

  return response.json();
}

/**
 * Base fetch function with error handling.
 */
async function fetchApi<T>(
  url: string,
  options?: RequestInit
): Promise<T> {
  const fullUrl = `${API_BASE_URL}${url}`;

  try {
    const response = await fetch(fullUrl, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options?.headers,
      },
    });

    return parseResponse<T>(response);
  } catch (error) {
    if (error instanceof Error) {
      // Re-throw with status info if available
      throw error;
    }
    throw new Error('Network error');
  }
}

/**
 * API client functions.
 */
export const apiClient = {
  // Ticket endpoints
  getTickets: (params: PaginationParams = {}) => {
    const searchParams = new URLSearchParams();
    if (params.page !== undefined) searchParams.append('page', params.page.toString());
    if (params.size !== undefined) searchParams.append('size', params.size.toString());
    if (params.search) searchParams.append('search', params.search);
    if (params.status) searchParams.append('status', params.status);

    const url = `${endpoints.tickets()}?${searchParams.toString()}`;
    return fetchApi<any>(url);
  },

  getTicket: (id: number | string) => {
    return fetchApi<any>(endpoints.ticket(id));
  },

  createTicket: (data: { title: string; description: string; priority: string; assignee?: string | null }) => {
    return fetchApi<any>(endpoints.tickets(), {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  updateTicket: (
    id: number | string,
    data: { title?: string; description?: string; priority?: string; assignee?: string | null }
  ) => {
    return fetchApi<any>(endpoints.ticket(id), {
      method: 'PATCH',
      body: JSON.stringify(data),
    });
  },

  transitionTicket: (id: number | string, data: { targetStatus: string }) => {
    return fetchApi<any>(endpoints.transition(id), {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  // Comment endpoints
  getComments: (ticketId: number | string) => {
    return fetchApi<any[]>(endpoints.comments(ticketId));
  },

  addComment: (
    ticketId: number | string,
    data: { text: string; author: string }
  ) => {
    return fetchApi<any>(endpoints.comments(ticketId), {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },
};
