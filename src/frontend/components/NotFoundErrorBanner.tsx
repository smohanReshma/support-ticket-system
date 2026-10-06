'use client';

import { ErrorDisplay } from './ErrorDisplay';

interface NotFoundErrorBannerProps {
  resourceType: string;
  resourceId: string;
  onDismiss?: () => void;
  onGoHome?: () => void;
}

export function NotFoundErrorBanner({
  resourceType,
  resourceId,
  onDismiss,
  onGoHome,
}: NotFoundErrorBannerProps) {
  return (
    <ErrorDisplay
      type="not-found"
      title="Resource not found"
      message={`The requested ${resourceType} (ID: ${resourceId}) could not be found.`}
      details={
        <div className="flex gap-2">
          {onGoHome && (
            <button
              onClick={onGoHome}
              className="px-3 py-1 bg-blue-600 text-white rounded-md hover:bg-blue-700"
            >
              Go to Home
            </button>
          )}
          <button
            onClick={onDismiss}
            className="px-3 py-1 bg-gray-600 text-white rounded-md hover:bg-gray-700"
          >
            Dismiss
          </button>
        </div>
      }
      onDismiss={onDismiss}
    />
  );
}
