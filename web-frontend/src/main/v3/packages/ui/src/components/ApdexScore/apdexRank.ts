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
