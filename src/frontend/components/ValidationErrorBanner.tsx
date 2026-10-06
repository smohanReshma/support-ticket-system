'use client';

import { Violation } from '../types/ticket';
import { ErrorDisplay } from './ErrorDisplay';

interface ValidationErrorBannerProps {
  violations: Violation[];
  onDismiss?: () => void;
}

export function ValidationErrorBanner({
  violations,
  onDismiss,
}: ValidationErrorBannerProps) {
  return (
    <ErrorDisplay
      type="validation"
      title="Validation errors"
      message="Please fix the errors below before submitting."
      details={
        <ul className="list-disc list-inside">
          {violations.map((v, i) => (
            <li key={i}>
              <strong>{v.field}:</strong> {v.message}
            </li>
          ))}
        </ul>
      }
      onDismiss={onDismiss}
    />
  );
}
