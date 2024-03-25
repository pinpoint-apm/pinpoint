import React from 'react';
import ContentLoader from 'react-content-loader';

export interface InspectorAgentInfoSkeletonProps {}

export const InspectorAgentInfoSkeleton = (props: InspectorAgentInfoSkeletonProps) => {
  const rows = 3;
  const rowHeight = 40;
  const labelRectWidth = 120;
  const valueRectWidth = 300;
  const rectHeight = 20;
  const gap = 10;

  return (
    <ContentLoader viewBox={`0 0 1300 ${rowHeight * rows}`} {...props}>
      {Array(rows)
        .fill(' ')
        .map((_, i) => {
          const contentVerticalPosition = (startY: number) => startY + rectHeight * i + gap * i;
          return (
            <React.Fragment key={i}>
              <rect
                x="0"
                y={`${contentVerticalPosition(20)}`}
                rx="4"
                ry="4"
                width={labelRectWidth}
                height={rectHeight}
              />
              <rect
                x="130"
                y={`${contentVerticalPosition(20)}`}
                rx="4"
                ry="4"
                width={valueRectWidth}
                height={rectHeight}
              />
              <rect
                x="440"
                y={`${contentVerticalPosition(20)}`}
                rx="4"
                ry="4"
                width={labelRectWidth}
                height={rectHeight}
              />
              <rect
                x="570"
                y={`${contentVerticalPosition(20)}`}
                rx="4"
                ry="4"
                width={valueRectWidth}
                height={rectHeight}
              />
              <rect
                x="880"
                y={`${contentVerticalPosition(20)}`}
                rx="4"
                ry="4"
                width={labelRectWidth}
                height={rectHeight}
              />
              <rect
                x="1010"
                y={`${contentVerticalPosition(20)}`}
                rx="4"
                ry="4"
                width={valueRectWidth}
                height={rectHeight}
              />
            </React.Fragment>
          );
        })}
    </ContentLoader>
  );
};
