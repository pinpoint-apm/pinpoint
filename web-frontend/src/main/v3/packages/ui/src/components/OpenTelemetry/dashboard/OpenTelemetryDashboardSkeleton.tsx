import React from 'react';
import ContentLoader from 'react-content-loader';

export interface OpenTelemetryDashboardSkeletonProps {}

export const OpenTelemetryDashboardSkeleton = () => {
  const buttonWidth = 110;
  const buttonHeight = 28;
  const buttonGap = 10;
  const headerGap = 12;
  const gridGap = 12;
  const metricHeight = 200;

  return (
    <ContentLoader width={'100%'} height={960}>
      {/* button groups */}
      <Rect
        x={0}
        y={0}
        style={{ transform: `translateX(calc(100% - ${buttonWidth}px))` }}
        width={buttonWidth}
        height={buttonHeight}
      />
      <Rect
        x={0}
        y={0}
        style={{ transform: `translateX(calc(100% - ${buttonWidth * 2 + buttonGap}px))` }}
        width={buttonWidth}
        height={buttonHeight}
      />
      {/* grid layout 1st line */}
      <Rect
        x={0}
        y={buttonHeight + headerGap}
        height={metricHeight}
        style={{ width: `calc((100% - ${gridGap * 2}px)/4)` }}
      />
      <Rect
        x={0}
        y={buttonHeight + headerGap}
        height={metricHeight}
        style={{
          width: `calc((100% - ${gridGap * 2}px)/4)`,
          transform: `translateX(calc((100% - ${gridGap * 2}px)/4 + ${gridGap}px))`,
        }}
      />
      <Rect
        x={0}
        y={buttonHeight + headerGap}
        height={metricHeight}
        style={{
          width: `calc((100% - ${gridGap * 2}px)/2)`,
          transform: `translateX(calc((100% - ${gridGap * 2}px)/2 + ${gridGap * 2}px))`,
        }}
      />
      {/* grid layout 2nd line */}
      <Rect
        x={0}
        y={buttonHeight + headerGap + metricHeight + gridGap}
        height={metricHeight}
        style={{
          width: `calc((100% - ${gridGap * 2}px)/2)`,
        }}
      />
      <Rect
        x={0}
        y={buttonHeight + headerGap + metricHeight + gridGap}
        height={metricHeight}
        style={{
          width: `calc((100% - ${gridGap * 2}px)/4)`,
          transform: `translateX(calc((100% - ${gridGap * 2}px)/2 + ${gridGap}px))`,
        }}
      />
      <Rect
        x={0}
        y={buttonHeight + headerGap + metricHeight + gridGap}
        height={metricHeight}
        style={{
          width: `calc((100% - ${gridGap * 2}px)/4)`,
          transform: `translateX(calc((100% - ${gridGap * 2}px) / 4 * 3 + ${gridGap * 2}px))`,
        }}
      />
      {/* grid layout 3nd line */}
      <Rect
        x={0}
        y={buttonHeight + headerGap + metricHeight * 2 + gridGap * 2}
        height={metricHeight}
        style={{
          width: '66%',
        }}
      />
    </ContentLoader>
  );
};

const Rect = (props: React.SVGProps<SVGRectElement>) => {
  return <rect rx="4" ry="4" {...props} />;
};
