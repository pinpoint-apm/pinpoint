import { Link } from 'react-router-dom';
import { ApplicationType } from '@pinpoint-fe/ui/src/constants';
import { useSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import { Button } from '../ui';
import { cn } from '../../lib';
import { ServerIcon } from '../Application/ServerIcon';

export const ApplicationLinkButton = () => {
  const { application, searchParameters, pathname } = useSearchParameters();
  const selectedAgentId = searchParameters.agentId;

  const removeAgentIdFromPath = () => {
    const urlSearchParams = new URLSearchParams(searchParameters);
    urlSearchParams.delete('agentId');

    return `${pathname}?${urlSearchParams.toString()}`;
  };

  return (
    <Button
      variant="ghost"
      asChild
      className="flex items-center w-full h-10 gap-1 px-1 rounded-none shrink-0 justify-normal"
    >
      <Link to={removeAgentIdFromPath()}>
        <ServerIcon application={application as ApplicationType} className="w-5 mx-1" />
        <div
          className={cn('text-sm font-semibold truncate', {
            'text-primary': !selectedAgentId,
          })}
        >
          {application?.applicationName}
        </div>
      </Link>
    </Button>
  );
};
