import { AgentSearchList } from '../../Agent';
import { useUrlStatSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import {
  convertParamsToQueryString,
  getFormattedDateRange,
  getUrlStatPath,
} from '@pinpoint-fe/ui/src/utils';
import { ApplicationLinkButton } from '../../Button/ApplicationLinkButton';
import { Separator } from '../../ui';
import { useNavigate } from 'react-router-dom';

export const UrlSidebar = () => {
  const navigate = useNavigate();
  const { application, dateRange, agentId } = useUrlStatSearchParameters();
  return (
    <div className="w-auto h-full min-w-auto">
      <ApplicationLinkButton />
      <Separator />
      <AgentSearchList
        selectedAgentId={agentId}
        onClickAgent={(agent) => {
          navigate(
            `${getUrlStatPath(application)}?${convertParamsToQueryString({
              ...getFormattedDateRange(dateRange),
              agentId: agentId === agent?.agentId ? '' : agent?.agentId,
            })}`,
          );
        }}
      />
    </div>
  );
};
