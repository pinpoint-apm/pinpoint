import { AgentSearchList } from '../../Agent';
import { useInspectorSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import {
  convertParamsToQueryString,
  getFormattedDateRange,
  getInspectorPath,
} from '@pinpoint-fe/ui/src/utils';
import { ApplicationLinkButton } from '../../Button/ApplicationLinkButton';
import { Separator } from '../../ui';
import { useNavigate } from 'react-router-dom';

export const InspectorSidebar = () => {
  const navigate = useNavigate();
  const { application, dateRange, agentId, version } = useInspectorSearchParameters();
  return (
    <div className="w-auto h-full min-w-auto">
      <ApplicationLinkButton />
      <Separator />
      <AgentSearchList
        selectedAgentId={agentId}
        onClickAgent={(agent) => {
          navigate(
            `${getInspectorPath(application)}?${convertParamsToQueryString({
              ...getFormattedDateRange(dateRange),
              agentId: agentId === agent?.agentId ? '' : agent?.agentId,
              version,
            })}`,
          );
        }}
      />
    </div>
  );
};
