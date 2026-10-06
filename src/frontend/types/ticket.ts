export interface Ticket {
  id: number;
  title: string;
  description: string;
  priority: string;
  status: string;
  assignee?: string | null;
  createdAt: string;
  updatedAt: string;
  comments?: Comment[];
}

export interface Comment {
  id: number;
  ticketId: number;
  text: string;
  author: string;
  createdAt: string;
}

export interface TicketListResponse {
  content: Ticket[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface Violation {
  field: string;
  message: string;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  violations?: Violation[];
  currentStatus?: string;
  requestedStatus?: string;
  validTransitions?: string[];
}
