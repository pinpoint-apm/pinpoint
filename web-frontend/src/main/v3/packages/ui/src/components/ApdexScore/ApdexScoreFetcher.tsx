import { Separator } from '../ui/separator';
import {
  useGetApdexScore,
  UseGetApdexScoreProps,
  useServerMapSearchParameters,
} from '@pinpoint-fe/ui/src/hooks';
import { HelpPopover } from '..';
import { GetServerMap } from '@pinpoint-fe/ui/src/constants';
import { ApdexScoreValue } from './ApdexScoreValue';

// 기간은 호출부에서 받지 않고 여기서 URL을 읽는다. 조회 훅은 URL과 분리해 두어야 하므로
// (`.claude/rules/api-hooks.md`) 읽는 자리를 컴포넌트로 내렸다.
export interface ApdexScoreFetcherProps extends Omit<UseGetApdexScoreProps, 'dateRange'> {}

export const ApdexScoreFetcher = (props: ApdexScoreFetcherProps) => {
  const { dateRange } = useServerMapSearchParameters();
  const { data } = useGetApdexScore({ ...props, dateRange });

  return (
    <div className="flex items-center h-full gap-1">
      Apdex
      {/* 색과 자릿수는 병합 노드 목록·service group 목록과 같은 규칙을 지나야 한다. */}
      <ApdexScoreValue apdex={data} />
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
