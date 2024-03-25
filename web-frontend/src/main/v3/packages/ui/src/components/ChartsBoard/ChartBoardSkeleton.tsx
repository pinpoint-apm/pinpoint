import ContentLoader from 'react-content-loader';
import { ScatterChartSkeletonContent } from '../ScatterChart/ScatterChartSkeleton';

export interface ChartBoardSkeleton {}

export const ChartBoardSkeleton = (props: ChartBoardSkeleton) => (
  <ContentLoader
    speed={2}
    width={'100%'}
    height={1300}
    // viewBox="0 0 500 1300"
    backgroundColor="#ebebeb"
    foregroundColor="#f5f5f5"
    {...props}
  >
    <rect x="15" y="10" rx="4" ry="4" width="50%" height="32" />
    <ScatterChartSkeletonContent x={25} y={75} />
    <rect x="25" y="420" rx="4" ry="4" width="50%" height="24" />
    <rect x="35" y="460" rx="5" ry="5" width="80%" height="120" />
    <rect x="25" y="640" rx="4" ry="4" width="40%" height="24" />
    <rect x="35" y="680" rx="5" ry="5" width="80%" height="120" />
    <rect x="25" y="860" rx="4" ry="4" width="30%" height="24" />
    <rect x="35" y="900" rx="5" ry="5" width="80%" height="120" />
    <rect x="25" y="1080" rx="4" ry="4" width="50%" height="24" />
    <rect x="35" y="1120" rx="5" ry="5" width="80%" height="120" />
  </ContentLoader>
);
