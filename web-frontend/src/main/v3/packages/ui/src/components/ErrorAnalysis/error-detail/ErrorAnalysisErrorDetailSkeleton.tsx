import React from 'react';
import ContentLoader from 'react-content-loader';

export interface ErrorAnalysisErrorDetailSkeletonProps {}

export const ErrorAnalysisErrorDetailSkeleton = () => {
  const elementRef = React.useRef<HTMLDivElement>(null);
  const [skeletonCount, setSkeletonCount] = React.useState<number>(1);
  const viewBoxWidth = 1000;
  const viewBoxHeight = 480;

  React.useEffect(() => {
    const elementWidth = elementRef.current?.offsetWidth || 0;
    const elementHeight = elementRef.current?.offsetWidth || 0;
    const areaHeight = (viewBoxHeight / viewBoxWidth) * elementWidth || 0;
    setSkeletonCount(Math.floor(elementHeight / areaHeight));
  }, []);

  return (
    <div className="flex flex-col h-full overflow-auto" ref={elementRef}>
      {Array(skeletonCount)
        .fill(0)
        .map((_, i) => (
          <div key={i}>
            <ContentLoader speed={2} viewBox={`0 0 ${viewBoxWidth} ${viewBoxHeight}`}>
              <ErrorDetailSkeleton />
            </ContentLoader>
          </div>
        ))}
    </div>
  );
};

const ErrorDetailSkeleton = () => {
  const padding = 24;
  const startX = padding;
  const startY = padding;
  const smallRectWidth = 200;
  const smallRectHeight = 20;
  const bigRectWidth = 900;
  const bigRectHeight = 350;
  const gap = 12;

  return (
    <React.Fragment>
      <rect x={startX} y={startY} rx="4" ry="4" width={smallRectWidth} height={smallRectHeight} />
      <rect
        x={startX}
        y={startY + smallRectHeight + gap}
        rx="4"
        ry="4"
        width={bigRectWidth}
        height={smallRectHeight}
      />
      <rect
        x={startX}
        y={startY + smallRectHeight * 2 + gap * 2}
        rx="4"
        ry="4"
        width={smallRectWidth}
        height={smallRectHeight}
      />
      <rect
        x={startX}
        y={startY + smallRectHeight * 3 + gap * 3}
        rx="4"
        ry="4"
        width={bigRectWidth}
        height={bigRectHeight}
      />
    </React.Fragment>
  );
};
