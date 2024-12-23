import { AgentSearchList } from '../../Agent';
import { useOpenTelemetrySearchParameters } from '@pinpoint-fe/ui/hooks';
import {
  convertParamsToQueryString,
  getFormattedDateRange,
  getOpenTelemetryPath,
} from '@pinpoint-fe/ui/utils';
import { ApplicationLinkButton } from '../../Button/ApplicationLinkButton';
import { Separator } from '../../ui';
import { useNavigate } from 'react-router-dom';

export const OpenTelemetrySidebar = () => {
  const navigate = useNavigate();
  const { application, dateRange, agentId } = useOpenTelemetrySearchParameters();
  return (
    <div className="w-60 min-w-[15rem] border-r-1 h-full">
      <ApplicationLinkButton />
      <Separator />
      <AgentSearchList
        selectedAgentId={agentId}
        onClickAgent={(agent) => {
          navigate(
            `${getOpenTelemetryPath(application)}?${convertParamsToQueryString({
              ...getFormattedDateRange(dateRange),
              agentId: agentId === agent?.agentId ? '' : agent?.agentId,
            })}`,
          );
        }}
      />
    </div>
  );
};
