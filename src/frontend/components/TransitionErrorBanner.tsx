'use client';

import { ReactNode } from 'react';
import { ErrorDisplay } from './ErrorDisplay';

interface TransitionError {
  currentStatus: string;
  requestedStatus: string;
  validTransitions: string[];
}

interface TransitionErrorBannerProps {
  error: TransitionError;
  onDismiss?: () => void;
}

export function TransitionErrorBanner({
  error,
  onDismiss,
}: TransitionErrorBannerProps) {
  return (
    <ErrorDisplay
      type="transition"
      title={`Cannot transition from ${error.currentStatus} to ${error.requestedStatus}`}
      message="The requested state transition is not allowed."
      details={
        <div>
          <p className="text-sm">
            Valid transitions from {error.currentStatus}:{' '}
            <span className="font-medium">
              {error.validTransitions.join(', ')}
            </span>
          </p>
        </div>
      }
      onDismiss={onDismiss}
    />
  );
}
