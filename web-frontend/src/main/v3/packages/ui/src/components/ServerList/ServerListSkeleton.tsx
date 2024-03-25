import React from 'react';
import ContentLoader from 'react-content-loader';

export interface ServerListSkeletonProps {
  count?: number;
  className?: string;
}
export const ServerListSkeleton = ({ count = 3, className }: ServerListSkeletonProps) => {
  const repeat = count;
  const child = 3;
  const repeatPadding = 16;
  const childPadding = 10;
  const parentHeight = 20;
  const childHeight = 16;

  return (
    <ContentLoader
      style={{ backgroundColor: 'var(--background-primary)' }}
      speed={2}
      backgroundColor="#ebebeb"
      foregroundColor="#f5f5f5"
      className={className}
    >
      {[...Array(repeat)].map((_, i) => {
        const py = 15 + (parentHeight + repeatPadding + child * (childHeight + childPadding)) * i;
        return (
          <React.Fragment key={i}>
            <rect x="15" y={py} rx="3" ry="3" width="130" height={parentHeight} />
            {[...Array(child)].map((_, j) => {
              const cy = py + (childPadding + childHeight) * j + parentHeight + childPadding;
              return (
                <rect key={`_${j}`} x="30" y={cy} rx="3" ry="3" width="220" height={childHeight} />
              );
            })}
          </React.Fragment>
        );
      })}
    </ContentLoader>
  );
};
