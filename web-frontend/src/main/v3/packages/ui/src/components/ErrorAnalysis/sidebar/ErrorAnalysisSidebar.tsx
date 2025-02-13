import { ErrorAnalysisGroupBy } from './ErrorAnalysisGroupBy';
import { Separator } from '../../ui';
import { AgentSearchList } from '../../Agent';
import { useErrorAnalysisSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import {
  getErrorAnalysisPath,
  convertParamsToQueryString,
  getFormattedDateRange,
} from '@pinpoint-fe/ui/src/utils';
import { useNavigate } from 'react-router-dom';
import { ApplicationLinkButton } from '../../Button/ApplicationLinkButton';

export const ErrorAnalysisSidebar = () => {
  const navigate = useNavigate();
  const { agentId, groupBy, application, dateRange } = useErrorAnalysisSearchParameters();
  return (
    <div className="w-60 min-w-[15rem] border-r-1 h-full">
      <ApplicationLinkButton />
      <Separator />
      <ErrorAnalysisGroupBy />
      <Separator />
      <AgentSearchList
        selectedAgentId={agentId}
        onClickAgent={(agent) => {
          navigate(
            `${getErrorAnalysisPath(application)}?${convertParamsToQueryString({
              ...getFormattedDateRange(dateRange),
              groupBy,
              agentId: agentId === agent?.agentId ? '' : agent?.agentId,
            })}`,
          );
        }}
      />
    </div>
  );
};
