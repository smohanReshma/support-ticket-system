'use client';

interface StateTransitionControlProps {
  currentStatus: string;
  onTransition: (targetStatus: string) => void;
  loading?: boolean;
}

const VALID_TRANSITIONS: Record<string, string[]> = {
  OPEN: ['IN_PROGRESS', 'CANCELLED'],
  IN_PROGRESS: ['RESOLVED', 'CANCELLED'],
  RESOLVED: ['CLOSED'],
  CANCELLED: [],
  CLOSED: [],
};

export function StateTransitionControl({
  currentStatus,
  onTransition,
  loading,
}: StateTransitionControlProps) {
  const validTransitions = VALID_TRANSITIONS[currentStatus] || [];

  if (validTransitions.length === 0) {
    return (
      <div className="bg-gray-50 rounded-lg p-4">
        <span className="text-gray-500">
          No transitions available for {currentStatus} tickets.
        </span>
      </div>
    );
  }

  return (
    <div className="flex items-center gap-3">
      <label htmlFor="transition-select" className="text-sm font-medium text-gray-700">
        Transition to:
      </label>
      <select
        id="transition-select"
        onChange={(e) => onTransition(e.target.value)}
        disabled={loading}
        className="px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
        defaultValue=""
      >
        <option value="" disabled>
          Select a status...
        </option>
        {validTransitions.map((status) => (
          <option key={status} value={status}>
            {status}
          </option>
        ))}
      </select>
      <span className="text-sm text-gray-500">
        Current: <strong>{currentStatus}</strong>
      </span>
    </div>
  );
}
