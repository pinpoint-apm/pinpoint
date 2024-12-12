import { AgentSearchList } from '../../Agent';
import { useInspectorSearchParameters } from '@pinpoint-fe/ui/hooks';
import {
  convertParamsToQueryString,
  getFormattedDateRange,
  getInspectorPath,
} from '@pinpoint-fe/utils';
import { ApplicationLinkButton } from '../../Button/ApplicationLinkButton';
import { Separator } from '../../ui';
import { useNavigate } from 'react-router-dom';

export const InspectorSidebar = () => {
  const navigate = useNavigate();
  const { application, dateRange, agentId, version } = useInspectorSearchParameters();
  return (
    <div className="w-60 min-w-[15rem] border-r-1 h-full">
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
