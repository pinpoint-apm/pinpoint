import ContentLoader from 'react-content-loader';

export interface TimelineSkeletonProps {}

export const TimelineSkeleton = () => {
  return (
    <ContentLoader viewBox="0 0 1300 80">
      <rect x="10" y="20" rx="4" ry="4" width="1280" height="40" />
    </ContentLoader>
  );
};
