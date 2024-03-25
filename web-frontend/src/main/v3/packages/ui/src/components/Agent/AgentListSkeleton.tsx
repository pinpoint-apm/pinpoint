import React from 'react';
import ContentLoader from 'react-content-loader';

export interface AgentListSkeletonProps {
  count?: number;
  className?: string;
}
export const AgentListSkeleton = ({ count = 3, className }: AgentListSkeletonProps) => {
  const repeat = count;
  const child = 3;
  const viewBoxWidth = 300;
  const viewBoxHeight = 500;
  const repeatPadding = 16;
  const parentXOffset = 16;
  const parentWidth = 130;
  const parentHeight = 30;
  const childPadding = 10;
  const chidWidth = 230;
  const childHeight = 20;
  const childXOffset = 30;

  return (
    <ContentLoader viewBox={`0 0 ${viewBoxWidth} ${viewBoxHeight}`} speed={2} className={className}>
      {[...Array(repeat)].map((_, i) => {
        const py = 15 + (parentHeight + repeatPadding + child * (childHeight + childPadding)) * i;
        return (
          <React.Fragment key={i}>
            <rect
              x={parentXOffset}
              y={py}
              rx="3"
              ry="3"
              width={parentWidth}
              height={parentHeight}
            />
            {[...Array(child)].map((_, j) => {
              const cy = py + (childPadding + childHeight) * j + parentHeight + childPadding;
              return (
                <rect
                  key={`_${j}`}
                  x={childXOffset}
                  y={cy}
                  rx="3"
                  ry="3"
                  width={chidWidth}
                  height={childHeight}
                />
              );
            })}
          </React.Fragment>
        );
      })}
    </ContentLoader>
  );
};
