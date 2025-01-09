import React from 'react';
import ContentLoader from 'react-content-loader';
import { DataTableSkeleton } from '../DataTable';
import { ChartSkeleton } from '../Chart';

export const AgentActiveThreadSkeleton = () => {
  const elementRef = React.useRef<HTMLDivElement>(null);

  return (
    <div
      className="flex w-[-webkit-fill-available] h-[-webkit-fill-available] overflow-hidden"
      ref={elementRef}
    >
      <div className="w-4/5 h-full">
        <ContentLoader speed={2} width={470} hanging={40} className="h-fit" viewBox={`0 0 470 100`}>
          <rect x={24} y={24} rx="4" ry="4" width={220} height={32} />
        </ContentLoader>
        <ChartSkeleton />
      </div>
      <div className="w-2/5">
        <ContentLoader speed={2} height={40} width={180}>
          <rect x={0} y={16} rx="4" ry="4" width={180} height={30} />
        </ContentLoader>
        <div className="flex flex-wrap gap-4 px-3 mt-3">
          <DataTableSkeleton hideRowBox />
        </div>
      </div>
    </div>
  );
};
