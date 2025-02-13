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
    <div className="w-60 min-w-[15rem] border-r-1 h-full">
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
