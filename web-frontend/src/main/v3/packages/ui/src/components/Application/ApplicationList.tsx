import React from 'react';
import { getServerIconPath } from '@pinpoint-fe/utils';
import { ApplicationType } from '@pinpoint-fe/constants';
import { ListItemSkeleton, VirtualList, VirtualListProps } from '../VirtualList';
import { ApplicationListFetcher } from '.';
import { ErrorBoundary } from '../Error';

export interface ApplicationVirtualListProps extends VirtualListProps<ApplicationType> {}

export const ApplicationVirtualList = ({ ...props }: ApplicationVirtualListProps) => {
  return <VirtualList {...props} filterKey="applicationName"></VirtualList>;
};

export const ApplicationItem = (application: ApplicationType) => {
  return (
    <>
      <img
        width={20}
        height="auto"
        alt={'application image'}
        src={getServerIconPath(application)}
      />
      <div className="truncate">{application.applicationName}</div>
    </>
  );
};

export const ApplicationList = (props: ApplicationVirtualListProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense
        fallback={
          <div className="h-48">
            <ListItemSkeleton skeletonOption={{ viewBoxHeight: 192 }} />
          </div>
        }
      >
        <ApplicationListFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
