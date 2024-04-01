import React from 'react';
import { ErrorBoundary } from '../Error/ErrorBoundary';
import { ThreadDumpListFecther, ThreadDumpListFectherProps } from './ThreadDumpListFecther';
import { ThreadDumpListSkeleton } from './ThreadDumpListSkeleton';

export interface ThreadDumpListProps extends ThreadDumpListFectherProps {}

export const ThreadDumpList = (props: ThreadDumpListProps) => {
  return (
    <ErrorBoundary
      errorMessage={(message) => (
        <div className="flex flex-col gap-1 text-sm">
          <div className="mb-2 font-semibold">{message}</div>
          <p>For some reason, this agent does not support thread dump.</p>
          <ul>
            <li>1. Check if this agent version is 1.6.1+</li>
            <li>2. Check if cluster feature is enabled.</li>
          </ul>
        </div>
      )}
    >
      <React.Suspense fallback={<ThreadDumpListSkeleton />}>
        <ThreadDumpListFecther {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
