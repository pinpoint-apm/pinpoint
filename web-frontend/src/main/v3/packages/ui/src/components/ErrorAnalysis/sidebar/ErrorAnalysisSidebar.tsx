import { ErrorAnalysisGroupBy } from './ErrorAnalysisGroupBy';
import { Separator } from '../../ui';
import { AgentSearchList } from '../../Agent';
import { useErrorAnalysisSearchParameters, useRequestService } from '@pinpoint-fe/ui/src/hooks';
import {
  getErrorAnalysisPath,
  convertParamsToQueryString,
  getFormattedDateRange,
} from '@pinpoint-fe/ui/src/utils';
import { useNavigate } from 'react-router';
import { ApplicationLinkButton } from '../../Button/ApplicationLinkButton';

export const ErrorAnalysisSidebar = () => {
  const navigate = useNavigate();
  const { agentId, groupBy, application, dateRange } = useErrorAnalysisSearchParameters();
  const serviceName = useRequestService();
  return (
    <div className="w-auto h-full min-w-auto">
      <ApplicationLinkButton />
      <Separator />
      <ErrorAnalysisGroupBy />
      <Separator />
      <AgentSearchList
        selectedAgentId={agentId}
        onClickAgent={(agent) => {
          navigate(
            `${getErrorAnalysisPath(application, undefined, serviceName)}?${convertParamsToQueryString(
              {
                ...getFormattedDateRange(dateRange),
                groupBy,
                agentId: agentId === agent?.agentId ? '' : agent?.agentId,
              },
            )}`,
          );
        }}
      />
    </div>
  );
};
