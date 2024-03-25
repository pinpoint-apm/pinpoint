import ContentLoader from 'react-content-loader';

export interface ListItemSkeletonProps {
  className?: string;
  skeletonOption?: {
    viewBoxWidth?: number;
    viewBoxHeight?: number;
    itemWidth?: number;
    itemHeight?: number;
    xPadding?: number;
    yPadding?: number;
    radius?: number;
    count?: number;
    gap?: number;
  };
}

const defaultOption = {
  viewBoxWidth: 320,
  viewBoxHeight: 235,
  itemWidth: 300,
  itemHeight: 20,
  xPadding: 10,
  yPadding: 10,
  radius: 5,
  count: 10,
  gap: 10,
};

export const ListItemSkeleton = ({ className, skeletonOption }: ListItemSkeletonProps) => {
  const {
    viewBoxWidth,
    viewBoxHeight,
    itemWidth,
    itemHeight,
    xPadding,
    yPadding,
    radius,
    count,
    gap,
  } = {
    ...defaultOption,
    ...skeletonOption,
  };

  return (
    <ContentLoader
      width={'100%'}
      height={'100%'}
      speed={2}
      backgroundColor="#ebebeb"
      foregroundColor="#f5f5f5"
      viewBox={`0 0 ${viewBoxWidth} ${viewBoxHeight}`}
      className={className}
    >
      {[...Array(count)].map((_, i) => {
        return (
          <rect
            key={i}
            x={xPadding}
            y={yPadding + gap * i + itemHeight * i}
            rx={radius}
            ry={radius}
            width={itemWidth}
            height={itemHeight}
          />
        );
      })}
    </ContentLoader>
  );
};
