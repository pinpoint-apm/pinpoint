import React from 'react';
import ContentLoader from 'react-content-loader';

export interface ServerMapSkeletonProps {
  className?: string;
}

export const ServerMapSkeleton = React.memo((props: ServerMapSkeletonProps) => {
  // 1850 * 1250
  // 975 * 625
  const random = Math.floor(Math.random() * 3) + 1;
  const renderRandomNodes = () => {
    if (random === 1) return <ThreeNodes />;
    if (random === 2) return <FiveNodes />;
    if (random === 3) return <SixNodes />;
  };

  return (
    <ContentLoader
      speed={2}
      width={975}
      height={625}
      viewBox="0 0 975 625"
      backgroundColor="#ebebeb"
      foregroundColor="#f5f5f5"
      {...props}
    >
      {renderRandomNodes()}
    </ContentLoader>
  );
});

const ThreeNodes = () => {
  return (
    <>
      <circle cx="627" cy="317" r="30" />
      <rect x="510" y="315" height="4" width="100" />
      <circle cx="487" cy="317" r="30" />
      <rect x="370" y="315" height="4" width="100" />
      <circle cx="347" cy="317" r="30" />
    </>
  );
};

const FiveNodes = () => {
  return (
    <>
      <circle cx="347" cy="197" r="30" />
      <rect x="0" y="0" height="4" width="135" transform="translate(367,215) rotate(40)" />
      <circle cx="347" cy="317" r="30" />
      <rect x="0" y="0" height="4" width="135" transform="translate(367,415) rotate(-40)" />
      <circle cx="347" cy="437" r="30" />
      <rect x="370" y="315" height="4" width="100" />
      <circle cx="487" cy="317" r="30" />
      <rect x="510" y="315" height="4" width="100" />
      <circle cx="627" cy="317" r="30" />
    </>
  );
};

const SixNodes = () => {
  return (
    <>
      <circle cx="347" cy="197" r="30" />
      <rect x="0" y="0" height="4" width="135" transform="translate(367,215) rotate(40)" />
      <circle cx="347" cy="317" r="30" />
      <rect x="0" y="0" height="4" width="135" transform="translate(367,415) rotate(-40)" />
      <circle cx="347" cy="437" r="30" />
      <rect x="370" y="315" height="4" width="100" />
      <circle cx="487" cy="317" r="30" />
      <rect x="0" y="0" height="4" width="120" transform="translate(607,267) rotate(155)" />
      <circle cx="627" cy="257" r="30" />
      <rect x="0" y="0" height="4" width="120" transform="translate(607,377) rotate(-155)" />
      <circle cx="627" cy="377" r="30" />
    </>
  );
};
