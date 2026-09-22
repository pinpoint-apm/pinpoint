import { cn } from '../../lib/utils';
import { ApdexScoreLike, RankColorClassNameMap, getApdexRank } from './apdexRank';

export interface ApdexScoreValueProps {
  apdex?: ApdexScoreLike;
  className?: string;
}

/**
 * Apdex 점수 하나를 등급 색으로 보여준다. **점수를 보여주는 모든 곳이 이 컴포넌트를 지난다**
 * (ChartsBoard의 `ApdexScoreFetcher`, 병합 노드 목록, service group 목록) — 같은 노드의 같은
 * 수치가 화면마다 다른 색·다른 자릿수로 보이면 사용자는 어느 쪽이 맞는지 알 수 없다.
 *
 * - 등급은 `getApdexRank`로 매긴다. 표본이 없어 0으로 내려온 값은 실패가 아니므로 빨강으로
 *   칠하지 않는다(그 판단의 근거는 `apdexRank.ts`에 적어 두었다).
 * - 표기는 map 노드 라벨과 같이 버림(`Math.floor`)으로 맞춘다. 반올림하면 0.937이 0.94가 되어
 *   색은 Good인데 숫자는 Excellent 경계값으로 읽힌다.
 *
 * 점수의 출처는 호출부가 정한다. map 목록은 이미 받아 둔 응답(`NodeData.apdex`)을 그대로 넘겨
 * `/getApdexScore`를 다시 부르지 않는다 — 목록에 있는 노드 수만큼 조회가 나가는데, 같은 기간의
 * 같은 수치를 map이 이미 실어 보내 준다.
 */
export const ApdexScoreValue = ({ apdex, className }: ApdexScoreValueProps) => {
  const score = apdex?.apdexScore || 0;

  return (
    <span className={cn('font-bold', RankColorClassNameMap[getApdexRank(apdex)], className)}>
      {(Math.floor(score * 100) / 100).toFixed(2)}
    </span>
  );
};
