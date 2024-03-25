import ContentLoader from 'react-content-loader';

export interface DataTableSkeletonProps {
  hideRowBox?: boolean;
}

export const DataTableSkeleton = ({ hideRowBox, ...props }: DataTableSkeletonProps) => {
  const viewBoxWidth = 1200;
  const viewBoxHeight = 500;
  const actionBoxWidth = 60;
  const actionBoxHeight = hideRowBox ? 0 : 30;
  const actionBoxGap = 15;
  const rowBoxWidth = viewBoxWidth;
  const rowBoxHeight = 40;
  const yOffset = actionBoxHeight;
  const rowCount = 9;
  const rowGap = 7;

  return (
    <ContentLoader
      speed={2}
      className="h-fit"
      viewBox={`0 0 ${viewBoxWidth} ${viewBoxHeight}`}
      {...props}
    >
      {!hideRowBox && (
        <rect
          x={viewBoxWidth - actionBoxWidth}
          y={yOffset}
          rx="4"
          ry="4"
          width={actionBoxWidth}
          height={actionBoxHeight}
        />
      )}
      {[...Array(rowCount)].map((_, i) => {
        return (
          <rect
            key={i}
            x={0}
            y={yOffset + actionBoxHeight + actionBoxGap + (rowBoxHeight + rowGap) * i}
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
