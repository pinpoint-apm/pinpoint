import ContentLoader from 'react-content-loader';

export interface ThreadDumpListSkeletonProps {}

export const ThreadDumpListSkeleton = ({ ...props }: ThreadDumpListSkeletonProps) => {
  const viewBoxWidth = 1200;
  const viewBoxHeight = 700;
  const rowBoxPadding = 6;
  const rowBoxWidth = viewBoxWidth - rowBoxPadding * 2;
  const rowBoxHeight = 16;
  const yOffset = 12;
  const rowCount = 15;

  return (
    <ContentLoader
      width={'100%'}
      height={viewBoxHeight}
      preserveAspectRatio="none"
      viewBox={`0 0 ${viewBoxWidth} ${viewBoxHeight}`}
      {...props}
    >
      {[...Array(rowCount)].map((_, i) => {
        return (
          <rect
            key={i}
            x={rowBoxPadding}
            y={yOffset + (rowBoxHeight + rowBoxPadding * 2) * i}
            rx="4"
            ry="4"
            width={rowBoxWidth}
            height={rowBoxHeight}
          />
        );
      })}
    </ContentLoader>
  );
};
