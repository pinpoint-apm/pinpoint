import ContentLoader from 'react-content-loader';

export interface TranstitleListSkeletonProps {}

export const TransactionListSkeleton = ({ ...props }: TranstitleListSkeletonProps) => {
  const viewBoxWidth = 1200;
  const viewBoxHeight = 700;
  // const headerXOffset = 10;
  const rowBoxPadding = 6;
  const rowBoxWidth = viewBoxWidth - rowBoxPadding * 2;
  const rowBoxHeight = 16;
  const yOffset = 12;
  const rowCount = 9;

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
