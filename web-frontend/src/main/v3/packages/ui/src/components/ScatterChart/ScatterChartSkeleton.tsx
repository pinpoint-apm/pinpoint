import ContentLoader from 'react-content-loader';

export interface ScatterChartSkeleton {}

export const ScatterChartSkeleton = (props: ScatterChartSkeleton) => (
  <ContentLoader
    speed={2}
    width={460}
    height={400}
    viewBox="0 0 460 400"
    backgroundColor="#ebebeb"
    foregroundColor="#f5f5f5"
    {...props}
  >
    <ScatterChartSkeletonContent y={30} />
  </ContentLoader>
);

export const ScatterChartSkeletonContent = ({ x = 0, y = 0 }: { x?: number; y?: number }) => (
  <>
    <rect x={x} y={y} rx="2" ry="2" width="12" height="286" />
    <rect x={x} y={275 + y} rx="2" ry="2" width="430" height="12" />
    <circle cx={77 + x} cy={193 + y} r="20" />
    <circle cx={152 + x} cy={110 + y} r="27" />
    <circle cx={339 + x} cy={106 + y} r="17" />
    <circle cx={233 + x} cy={142 + y} r="18" />
    <circle cx={262 + x} cy={80 + y} r="16" />
    <circle cx={163 + x} cy={202 + y} r="19" />
    <circle cx={267 + x} cy={197 + y} r="16" />
    <circle cx={313 + x} cy={154 + y} r="19" />
  </>
);
