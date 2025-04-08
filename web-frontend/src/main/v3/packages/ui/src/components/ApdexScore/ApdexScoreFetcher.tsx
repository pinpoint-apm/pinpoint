import { Separator } from '../ui/separator';
import {
  useGetApdexScore,
  UseGetApdexScoreProps,
  useGetApplicationList,
} from '@pinpoint-fe/ui/src/hooks';
import { HelpPopover } from '..';
import { ApplicationType, GetServerMap } from '@pinpoint-fe/ui/src/constants';
import React from 'react';

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
  const serviceTypeCode = props?.nodeData?.serviceTypeCode;
  const { data: applicationListData } = useGetApplicationList(!serviceTypeCode);

  const params = React.useMemo(() => {
    if (serviceTypeCode) {
      return props;
    }

    if (applicationListData) {
      const code = applicationListData.find(
        (appData: ApplicationType) =>
          appData?.applicationName === props?.nodeData?.applicationName &&
          appData?.serviceType === props?.nodeData?.serviceType,
      )?.code;

      if (!code) {
        return {};
      }

      return {
        ...props,
        nodeData: {
          ...props.nodeData,
          serviceTypeCode: code,
        },
      };
    }

    return {};
  }, [props, serviceTypeCode, applicationListData]);

  const { data } = useGetApdexScore(params as UseGetApdexScoreProps);

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
    <div className="flex items-center h-full gap-1">
      Apdex
      <div className={`font-bold ${RankColorClassNameMap[rank]}`}>
        {(Math.floor(score * 100) / 100).toFixed(2)}
      </div>
      <div>
        <HelpPopover
          helpKey="HELP_VIEWER.APDEX_SCORE"
          prevContent={
            <>
              <div className="flex flex-col gap-3 p-4 pt-2.5 text-xs">
                <div className="text-center">
                  <span>{data?.apdexFormula?.satisfiedCount || 0}</span>
                  <span>{` + [ `}</span>
                  <span>{data?.apdexFormula?.toleratingCount || 0}</span>
                  <span>{` / `}</span>
                  <span>{` 2 `}</span>
                  <span>{` ] `}</span>
                </div>
                <Separator />
                <div className="text-center">{data?.apdexFormula?.totalSamples || 0}</div>
              </div>
            </>
          }
        />
      </div>
    </div>
  );
};
