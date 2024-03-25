import React from 'react';
import ContentLoader from 'react-content-loader';

export interface ChartSkeletonProps {
  skeletonOption?: {
    viewBoxWidth?: number;
    viewBoxHeight?: number;
    rectCount?: number;
    rectWidth?: number;
    startX?: number;
    gap?: number;
    axisWidth?: number;
  };
}

const defaultOption = {
  viewBoxWidth: 1200,
  viewBoxHeight: 400,
  rectCount: 26,
  rectWidth: 40,
  startX: 20,
  gap: 5,
  axisWidth: 5,
};

export const ChartSkeleton = ({ skeletonOption, ...props }: ChartSkeletonProps) => {
  const { viewBoxWidth, viewBoxHeight, rectCount, rectWidth, startX, gap, axisWidth } = {
    ...defaultOption,
    ...skeletonOption,
  };
  const yAxisXOffset = startX - gap * 2 - axisWidth;
  const yAxisYOffset = 30;
  const yAxisHeight = viewBoxHeight * 0.9 - yAxisYOffset + axisWidth + gap * 2;
  const xAxisXOffset = startX - gap * 2;
  const xAxisYOffset = yAxisYOffset + yAxisHeight - axisWidth;
  const xAxisWidth = gap + rectWidth * rectCount + gap * (rectCount - 1) + 20;

  return (
    <ContentLoader
      width={'100%'}
      height={'100%'}
      viewBox={`0 0 ${viewBoxWidth} ${viewBoxHeight}`}
      {...props}
    >
      <rect
        x={yAxisXOffset}
        y={yAxisYOffset}
        rx="0"
        ry="0"
        width={axisWidth}
        height={yAxisHeight}
      />
      <rect x={xAxisXOffset} y={xAxisYOffset} rx="0" ry="0" width={xAxisWidth} height={axisWidth} />
      {Array(rectCount)
        .fill(0)
        .map((_, i) => {
          const rectY =
            Math.floor(yAxisYOffset + Math.random() * (viewBoxHeight / 2 - yAxisYOffset)) + 1;
          const rectHeight = (viewBoxHeight / 10) * 9 - rectY;

          return (
            <React.Fragment key={i}>
              <rect
                x={startX + rectWidth * i + gap * i}
                y={rectY}
                rx="0"
                ry="0"
                width={rectWidth}
                height={rectHeight}
              />
            </React.Fragment>
          );
        })}
    </ContentLoader>
  );
};
