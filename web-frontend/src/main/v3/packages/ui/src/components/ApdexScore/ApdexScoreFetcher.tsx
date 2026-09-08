import { Separator } from '../ui/separator';
import {
  useGetApdexScore,
  UseGetApdexScoreProps,
  useServerMapSearchParameters,
} from '@pinpoint-fe/ui/src/hooks';
import { HelpPopover } from '..';
import { GetServerMap } from '@pinpoint-fe/ui/src/constants';

// 기간은 호출부에서 받지 않고 여기서 URL을 읽는다. 조회 훅은 URL과 분리해 두어야 하므로
// (`.claude/rules/api-hooks.md`) 읽는 자리를 컴포넌트로 내렸다.
export interface ApdexScoreFetcherProps extends Omit<UseGetApdexScoreProps, 'dateRange'> {}

enum RANK {
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

export const ApdexScoreFetcher = (props: ApdexScoreFetcherProps) => {
  const { dateRange } = useServerMapSearchParameters();
  const { data } = useGetApdexScore({ ...props, dateRange });

  const score = data?.apdexScore || 0;

  const rank = data?.apdexScore ? getRank(score) : RANK.EXCELLENT;

  return (
    <div className="flex items-center h-full gap-1">
      Apdex
      <div className={`font-bold ${RankColorClassNameMap[rank]}`}>
        {(Math.floor(score * 100) / 100).toFixed(2)}
      </div>
      <div>
        <HelpPopover
          helpKey="HELP_VIEWER.APDEX_SCORE"
          prevContent={
            <ApdexScoreApdexFormula
              satisfiedCount={data?.apdexFormula?.satisfiedCount || 0}
              toleratingCount={data?.apdexFormula?.toleratingCount || 0}
              totalSamples={data?.apdexFormula?.totalSamples || 0}
            />
          }
        />
      </div>
    </div>
  );
};

export const ApdexScoreApdexFormula = ({
  satisfiedCount,
  toleratingCount,
  totalSamples,
}: GetServerMap.ApdexFormula) => {
  return (
    <>
      <div className="flex flex-col gap-3 p-4 pt-2.5 text-xs">
        <div className="text-center">
          <span>{satisfiedCount || 0}</span>
          <span>{` + [ `}</span>
          <span>{toleratingCount || 0}</span>
          <span>{` / `}</span>
          <span>{` 2 `}</span>
          <span>{` ] `}</span>
        </div>
        <Separator />
        <div className="text-center">{totalSamples || 0}</div>
      </div>
    </>
  );
};
