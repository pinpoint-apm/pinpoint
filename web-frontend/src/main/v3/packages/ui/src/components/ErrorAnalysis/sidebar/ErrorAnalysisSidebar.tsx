import { ErrorAnalysisGroupBy } from './ErrorAnalysisGroupBy';
import { Separator } from '../../ui';
import { AgentSearchList } from '../../Agent';
import { useErrorAnalysisSearchParameters } from '@pinpoint-fe/hooks';
import {
  getErrorAnalysisPath,
  convertParamsToQueryString,
  getFormattedDateRange,
} from '@pinpoint-fe/utils';
import { useNavigate } from 'react-router-dom';

export const ErrorAnalysisSidebar = () => {
  const navigate = useNavigate();
  const { agentId, groupBy, application, dateRange } = useErrorAnalysisSearchParameters();
  return (
    <div className="w-60 min-w-[15rem] border-r-1 h-full">
      <ErrorAnalysisGroupBy />
      <Separator />
      <AgentSearchList
        className="max-h-[calc(100%-10.25rem)]"
        selectedAgentId={agentId}
        onClickAgent={(agent) => {
          navigate(
            `${getErrorAnalysisPath(application)}?${convertParamsToQueryString({
              ...getFormattedDateRange(dateRange),
              groupBy,
              agentId: agentId === agent.agentId ? '' : agent.agentId,
            })}`,
          );
        }}
      />
    </div>
  );
};
