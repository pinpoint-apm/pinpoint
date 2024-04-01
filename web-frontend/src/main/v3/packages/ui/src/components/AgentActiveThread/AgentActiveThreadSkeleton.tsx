import React from 'react';
import ContentLoader from 'react-content-loader';

export const AgentActiveThreadSkeleton = () => {
  const elementRef = React.useRef<HTMLDivElement>(null);

  return (
    <div className="grid w-full grid-cols-[30rem_auto]" ref={elementRef}>
      <div>
        <ContentLoader speed={2} height="100%" width={470}>
          <rect x={24} y={24} rx="4" ry="4" width={220} height={32} />
          <rect x={24} y={70} rx="4" ry="4" width={300} height={156} />
        </ContentLoader>
      </div>
      <div>
        <ContentLoader speed={2} height={40} width={180}>
          <rect x={0} y={16} rx="4" ry="4" width={180} height={30} />
        </ContentLoader>
        <div className="flex flex-wrap gap-4 px-3 mt-3">
          {Array(8)
            .fill(0)
            .map((_, i) => (
              <div key={i} className="">
                <ContentLoader speed={2} height={130} width={180}>
                  <rect x={0} y={16} rx="4" ry="4" width={180} height={20} />
                  <rect x={0} y={52} rx="4" ry="4" width={180} height={76} />
                </ContentLoader>
              </div>
            ))}
        </div>
      </div>
    </div>
  );
};
