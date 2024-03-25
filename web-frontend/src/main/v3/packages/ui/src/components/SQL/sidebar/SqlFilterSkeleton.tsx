import ContentLoader from 'react-content-loader';

export interface SqlFilterSkeletonProps {}

export const SqlFilterSkeleton = (props: SqlFilterSkeletonProps) => {
  const viewBoxWidth = 300;
  const viewBoxHeight = 500;
  const xOffset = 20;
  const yOffset = 30;
  const titleBoxWidth = 80;
  const titleBoxHeight = 25;
  const titleBoxGap = 15;
  const rowBoxWidth = 260;
  const rowBoxHeight = 100;
  const rowGap = 15;
  const rowCount = 3;
  const rowHeights = [...Array(rowCount)].map(() => Math.random() * rowBoxHeight + rowBoxHeight);

  return (
    <ContentLoader
      speed={2}
      className="h-fit"
      viewBox={`0 0 ${viewBoxWidth} ${viewBoxHeight}`}
      {...props}
    >
      <rect x={xOffset} y={yOffset} rx="4" ry="4" width={titleBoxWidth} height={titleBoxHeight} />
      {rowHeights.map((height, i) => {
        const addedHeight =
          i === 0 ? 0 : [...Array(i)].reduce((acc, _, i) => acc + rowHeights[i], 0);

        return (
          <rect
            key={i}
            x={xOffset}
            y={yOffset + titleBoxHeight + titleBoxGap + rowGap * i + addedHeight}
            rx="4"
            ry="4"
            width={rowBoxWidth}
            height={height}
          />
        );
      })}
    </ContentLoader>
  );
};
