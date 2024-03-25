import ContentLoader from 'react-content-loader';

export interface TransactionInfoSkeletonProps {}

export const TransactionInfoSkeleton = ({ ...props }: TransactionInfoSkeletonProps) => {
  const viewBoxWidth = 1200;
  const viewBoxHeight = 700;
  // const headerXOffset = 10;
  const titleBoxWidth = 500;
  const titleBoxHeight = 20;
  const titleBoxGap = 15;
  const actionBoxWidth = 200;
  const actionBoxHeight = 36;
  const actionBoxGap = 15;
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
      <rect
        x={rowBoxPadding}
        y={yOffset}
        rx="4"
        ry="4"
        width={titleBoxWidth}
        height={titleBoxHeight}
      />
      <rect
        x={rowBoxPadding}
        y={yOffset + titleBoxHeight + titleBoxGap}
        rx="4"
        ry="4"
        width={actionBoxWidth}
        height={actionBoxHeight}
      />
      {[...Array(rowCount)].map((_, i) => {
        return (
          <rect
            key={i}
            x={rowBoxPadding}
            y={
              yOffset +
              titleBoxHeight +
              titleBoxGap +
              actionBoxGap +
              actionBoxHeight +
              (rowBoxHeight + rowBoxPadding * 2) * i
            }
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
