import { cn } from '../../lib/utils';
import { RankColorClassNameMap, getRank } from './apdexRank';

export interface ApdexScoreValueProps {
  score: number;
  className?: string;
}

/**
 * Apdex 점수 하나를 등급 색으로 보여준다.
 *
 * 색과 표기는 ChartsBoard의 Apdex(`ApdexScoreFetcher`)와 **같은 규칙을 지나야 한다** — 같은
 * 노드의 같은 수치가 화면마다 다른 색·다른 자릿수로 보이면 안 된다. 그래서 등급은 `getRank`로,
 * 표기는 map 노드 라벨과 같이 버림(`Math.floor`)으로 맞춘다(반올림하면 0.937이 0.94가 되어
 * 색은 Good인데 숫자는 Excellent 경계값으로 읽힌다).
 *
 * 점수는 호출부가 map 응답(`NodeData.apdex`)에서 그대로 넘긴다. `/getApdexScore`를 다시 부르지
 * 않는다 — 목록에 있는 노드 수만큼 조회가 나가는데, 같은 기간의 같은 수치를 map이 이미 실어
 * 보내 준다.
 */
export const ApdexScoreValue = ({ score, className }: ApdexScoreValueProps) => {
  return (
    <span className={cn('font-bold', RankColorClassNameMap[getRank(score)], className)}>
      {(Math.floor(score * 100) / 100).toFixed(2)}
    </span>
  );
};
