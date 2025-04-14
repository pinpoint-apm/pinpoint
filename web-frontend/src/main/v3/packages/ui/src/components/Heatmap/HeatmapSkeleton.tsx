import ContentLoader from 'react-content-loader';

export interface HeatmapSkeletonProps {
  skeletonOption?: {
    viewBoxWidth?: number;
    viewBoxHeight?: number;
    rowCount?: number;
    columnCount?: number;
    cellSize?: number;
    gap?: number;
    axisWidth?: number;
    startX?: number;
    startY?: number;
  };
}

const defaultOption = {
  viewBoxWidth: 1200,
  viewBoxHeight: 400,
  rowCount: 7,
  columnCount: 26,
  cellSize: 40,
  gap: 5,
  axisWidth: 5,
  startX: 50,
  startY: 30,
};

export const HeatmapSkeleton = ({ skeletonOption, ...props }: HeatmapSkeletonProps) => {
  const {
    viewBoxWidth,
    viewBoxHeight,
    rowCount,
    columnCount,
    cellSize,
    gap,
    axisWidth,
    startX,
    startY,
  } = { ...defaultOption, ...skeletonOption };

  const yAxisXOffset = startX - gap * 2 - axisWidth;
  const yAxisHeight = cellSize * rowCount + gap * (rowCount - 1);
  const xAxisYOffset = startY + yAxisHeight + gap;

  return (
    <ContentLoader
      width="100%"
      height="100%"
      viewBox={`0 0 ${viewBoxWidth} ${viewBoxHeight}`}
      {...props}
    >
      {/* Y Axis */}
      <rect x={yAxisXOffset} y={startY} width={axisWidth} height={yAxisHeight} />

      {/* X Axis */}
      <rect
        x={startX - gap}
        y={xAxisYOffset}
        width={cellSize * columnCount + gap * (columnCount - 1)}
        height={axisWidth}
      />

      {/* Heatmap Cells */}
      {Array.from({ length: rowCount }).flatMap((_, rowIdx) =>
        Array.from({ length: columnCount }).map((_, colIdx) => {
          const x = startX + colIdx * (cellSize + gap);
          const y = startY + rowIdx * (cellSize + gap);
          return (
            <rect key={`cell-${rowIdx}-${colIdx}`} x={x} y={y} width={cellSize} height={cellSize} />
          );
        }),
      )}
    </ContentLoader>
  );
};
