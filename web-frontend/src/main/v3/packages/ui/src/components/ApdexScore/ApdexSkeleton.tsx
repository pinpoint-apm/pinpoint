import ContentLoader from 'react-content-loader';

export interface ApdexSkeletonProps {}

export const ApdexSkeleton = () => {
  return (
    <ContentLoader speed={2} width={86} height={20} viewBox="0 0 86 20">
      <rect x="0" y="0" rx="2" ry="2" width="86" height="20" />
    </ContentLoader>
  );
};
