import React from 'react';
import { ApdexScoreFetcher, ApdexScoreFetcherProps } from './ApdexScoreFetcher';
import { ApdexSkeleton, ErrorBoundary } from '..';

export interface ApdexScoreProps extends ApdexScoreFetcherProps {}

export const ApdexScore = (props: ApdexScoreProps) => {
  // 조회 대상이 없으면 fetcher를 아예 마운트하지 않는다. `/getApdexScore`는 applicationName이
  // 필수라 없이 호출하면 400이고, 이 훅은 useSuspenseQuery를 쓸 수 있어 enabled로 막을 수 없다.
  // (nodeData는 상위에서 "선택된 노드 ?? 기준 application"으로 만들어지므로 둘 다 없을 때 비는데,
  //  service를 바꾼 직후처럼 선택도 기준 application도 없는 순간이 실제로 존재한다.)
  if (!props.nodeData?.applicationName || !props.nodeData?.serviceType) {
    return null;
  }

  return (
    <ErrorBoundary>
      <React.Suspense fallback={<ApdexSkeleton />}>
        <ApdexScoreFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
