import { Popover, PopoverContent, PopoverTrigger } from '../ui/popover';
import { FaSmileBeam, FaSmile, FaMeh, FaFrown, FaAngry } from 'react-icons/fa';
import { Separator } from '../ui/separator';
import { useGetApdexScore, UseGetApdexScoreProps } from '@pinpoint-fe/hooks';
import { Button } from '..';

export interface ApdexScoreFetcherProps extends UseGetApdexScoreProps {}

enum RANK {
  EXCELLENT,
  GOOD,
  FAIR,
  POOR,
  UNACCEPTABLE,
}

const RankColorClassNameMap: { [key: string]: string } = {
  [RANK.EXCELLENT]: 'text-status-success',
  [RANK.GOOD]: 'text-status-good',
  [RANK.POOR]: 'text-status-warn',
  [RANK.UNACCEPTABLE]: 'text-status-fail',
};

export const ApdexScoreFetcher = (props: ApdexScoreFetcherProps) => {
  const { data } = useGetApdexScore(props);
  const score = data?.apdexScore || 0;
  const getRank = () => {
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
  const rank = data?.apdexScore ? getRank() : RANK.EXCELLENT;

  return (
    <>
      <Popover>
        <PopoverTrigger>
          <Button variant="ghost" className="inline-flex h-full px-2 py-1">
            Apdex{' '}
            <div className={`font-bold ml-1.5 ${RankColorClassNameMap[rank]}`}>
              {(Math.floor(score * 100) / 100).toFixed(2)}
            </div>
          </Button>
        </PopoverTrigger>
        <PopoverContent className="w-64 px-0 z-[1100]">
          <div className="text-xs">
            <div className="px-4 text-base font-semibold text-primary">Apdex Score</div>
            <div className="flex flex-col gap-3 p-4 pt-2.5">
              <div className="text-center">
                <span>{data?.apdexFormula.satisfiedCount}</span>
                <span>{` + [ `}</span>
                <span>{data?.apdexFormula.toleratingCount}</span>
                <span>{` / `}</span>
                <span>{` 2 `}</span>
                <span>{` ] `}</span>
              </div>
              <Separator />
              <div className="text-center">{data?.apdexFormula.totalSamples}</div>
            </div>
            <div />
            <div className="h-0 p-0 border-b-1"></div>
            <div className="grid grid-cols-[50%_50%] gap-2 justify-items-center items-center px-5 py-4 [&>*:nth-child(even)]:justify-self-start">
              <div style={{ fontWeight: 'bold', padding: 7 }}>Score</div>
              <div></div>
              <div>0.94 ~ 1.00</div>
              <div>
                <FaSmileBeam className={RankColorClassNameMap[RANK.EXCELLENT]} /> Excellent
              </div>
              <div>0.85 ~ 0.94</div>
              <div>
                <FaSmile className={RankColorClassNameMap[RANK.GOOD]} /> Good
              </div>
              <div>0.7 ~ 0.85</div>
              <div>
                <FaMeh /> Fair
              </div>
              <div>0.5 ~ 0.7</div>
              <div>
                <FaFrown className={RankColorClassNameMap[RANK.POOR]} /> Poor
              </div>
              <div>&lt; 0.5</div>
              <div>
                <FaAngry className={RankColorClassNameMap[RANK.UNACCEPTABLE]} /> Unacceptable
              </div>
            </div>
          </div>
        </PopoverContent>
      </Popover>
    </>
  );
};
