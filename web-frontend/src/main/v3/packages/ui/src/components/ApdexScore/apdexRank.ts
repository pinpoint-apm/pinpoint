/**
 * Apdex 등급과 그 색. 점수를 보여주는 곳이 여럿이라(ChartsBoard, 병합 노드 목록, service group
 * 목록) **규칙은 여기 하나뿐이어야 한다** — 같은 수치가 화면마다 다른 색으로 보이면 사용자는
 * 어느 쪽이 맞는지 알 수 없다.
 *
 * 조회 훅을 끌고 오는 `ApdexScoreFetcher`가 아니라 별도 모듈에 둔다. 색만 필요한 표시용
 * 컴포넌트까지 fetcher의 의존성(hooks → echarts)을 함께 들여오게 되기 때문이다.
 */
export enum RANK {
  EXCELLENT,
  GOOD,
  FAIR,
  POOR,
  UNACCEPTABLE,
}

export const RankColorClassNameMap: { [key: string]: string } = {
  [RANK.EXCELLENT]: 'text-status-success',
  [RANK.GOOD]: 'text-status-good',
  [RANK.FAIR]: 'text-[#f7d84a]',
  [RANK.POOR]: 'text-status-warn',
  [RANK.UNACCEPTABLE]: 'text-status-fail',
};

/**
 * 점수를 보여주는 곳들이 받는 값의 공통 모양. `/getApdexScore` 응답(`GetApdexScore.Response`)과
 * map 응답의 노드(`GetServerMap.NodeData['apdex']`)가 같은 모양이라 그대로 넘길 수 있다.
 */
export interface ApdexScoreLike {
  apdexScore: number;
  apdexFormula?: { totalSamples: number };
}

export const getRank = (score: number) => {
  if (score >= 0.94) {
    return RANK.EXCELLENT;
  } else if (score >= 0.85) {
    return RANK.GOOD;
  } else if (score >= 0.7) {
    return RANK.FAIR;
  } else if (score >= 0.5) {
    return RANK.POOR;
  } else {
    return RANK.UNACCEPTABLE;
  }
};

/**
 * 표본이 하나도 없는 구간인지.
 *
 * 백엔드는 표본이 없으면 점수를 **0으로** 내려보낸다(`ApdexScore#calculateApdexScore` —
 * `totalSamples`가 0이면 0을 반환). 그래서 **점수만 보면 "데이터가 없다"와 "전부 느렸다"를 구분할
 * 수 없다.** 둘을 가르는 것은 `apdexFormula.totalSamples` 하나뿐이다.
 *
 * formula를 알 수 없을 때(조회 전이라 응답 자체가 없음)는 점수도 없는 것이므로 데이터 없음으로 본다.
 */
export const isApdexNoData = (apdex?: ApdexScoreLike) => {
  if (!apdex) {
    return true;
  }
  if (apdex.apdexFormula) {
    return apdex.apdexFormula.totalSamples === 0;
  }
  return !apdex.apdexScore;
};

/**
 * 점수 하나의 등급. **데이터가 없는 것은 실패가 아니므로 빨강으로 칠하지 않는다** — map 노드의
 * 링도 수집되지 않은 슬롯을 Excellent로 그린다(`getTimeSeriesApdexInfo`의 `APDEX_UNCOLLECTED`).
 * 등급을 매길 표본이 없는데 최하위로 칠하면, 트래픽이 없었을 뿐인 application이 장애로 읽힌다.
 *
 * 반대로 표본이 있는데 나온 0은 진짜 0이다(전부 느리거나 실패). 그것까지 데이터 없음으로 넘기면
 * 가장 나쁜 상태가 초록으로 보인다 — `apdexScore`의 참/거짓만으로 가르면 그렇게 된다.
 */
export const getApdexRank = (apdex?: ApdexScoreLike) =>
  isApdexNoData(apdex) ? RANK.EXCELLENT : getRank(apdex?.apdexScore ?? 0);
