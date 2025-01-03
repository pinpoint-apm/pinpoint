import {
  useErrorAnalysisSearchParameters,
  useGetErrorAnalysisGroupsData,
} from '@pinpoint-fe/ui/hooks';
import { cn } from '../../../lib';
import { Checkbox } from '../../ui';
import { Label } from '../../ui/label';
import { useNavigate } from 'react-router-dom';
import {
  getErrorAnalysisPath,
  convertParamsToQueryString,
  getFormattedDateRange,
} from '@pinpoint-fe/ui/utils';

export interface ErrorAnalysisGroupByFetcherProps {
  className?: string;
  emptyMessage?: string;
}

export const ErrorAnalysisGroupByFetcher = ({
  className,
  emptyMessage,
}: ErrorAnalysisGroupByFetcherProps) => {
  const navigate = useNavigate();
  const ID_PREFIX = 'error_analysis_groupby';
  const { agentId, application, dateRange, parsedGroupBy } = useErrorAnalysisSearchParameters();
  const { data } = useGetErrorAnalysisGroupsData();
  const selectedGroups = parsedGroupBy || [];

  return (
    <div className={cn('p-5 pb-6', className)}>
      <div className="font-semibold mb-3 text-sm">Group By</div>
      {data && data.length > 0 ? (
        <div className="flex flex-col gap-2">
          {data.map((option, i) => {
            return (
              <div className="flex items-center space-x-2" key={i}>
                <Checkbox
                  id={`${ID_PREFIX}_${option}`}
                  checked={selectedGroups?.includes(option)}
                  onCheckedChange={(checked) => {
                    let groupBy = '';
                    if (checked) {
                      groupBy = [...selectedGroups, option].join(',');
                    } else {
                      groupBy = selectedGroups
                        .filter((selectedGroup) => selectedGroup !== option)
                        .join(',');
                    }

                    navigate(
                      `${getErrorAnalysisPath(application)}?${convertParamsToQueryString({
                        ...getFormattedDateRange(dateRange),
                        agentId,
                        groupBy,
                      })}`,
                    );
                  }}
                />
                <Label className="text-xs" htmlFor={`${ID_PREFIX}_${option}`}>
                  {option}
                </Label>
              </div>
            );
          })}
        </div>
      ) : (
        <div className="flex justify-center opacity-50">{emptyMessage}</div>
      )}
    </div>
  );
};
