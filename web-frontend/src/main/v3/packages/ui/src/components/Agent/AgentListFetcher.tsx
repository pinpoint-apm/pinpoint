import React from 'react';
import Fuse from 'fuse.js';
import { RxCheck } from 'react-icons/rx';
import {
  AGENT_LIST_SORT_BY,
  getDateRange,
  useGetAgentOverview,
  useSearchParameters,
} from '@pinpoint-fe/ui/src/hooks';
import { AgentOverview, colors } from '@pinpoint-fe/ui/src/constants';
import { cn } from '../../lib/utils';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '../ui';
import { FaArrowAltCircleDown, FaExclamationCircle, FaTimesCircle } from 'react-icons/fa';
import { PiHardDriveFill } from 'react-icons/pi';

export interface AgentListFetcherProps extends Pick<React.HTMLAttributes<HTMLDivElement>, 'style'> {
  className?: string;
  sortBy?: AGENT_LIST_SORT_BY;
  filterKeyword?: string;
  selectedAgentId?: string;
  emptyMessage?: React.ReactNode;
  agentRenderer?: (agent: AgentOverview.Instance) => React.ReactNode;
  onClickAgent?: (agent?: AgentOverview.Instance) => void;
}

export const AgentListFetcher = ({
  className,
  sortBy,
  selectedAgentId,
  filterKeyword = '',
  emptyMessage = 'No Agents',
  agentRenderer,
  onClickAgent,
  style,
}: AgentListFetcherProps) => {
  const { search, application } = useSearchParameters();
  const dateRange = getDateRange(search, false);
  const from = dateRange.from.getTime();
  const to = dateRange.to.getTime();

  const { data } = useGetAgentOverview({
    application: application?.applicationName || '',
    serviceTypeName: application?.serviceType,
    from,
    to,
  });

  const filteredList = React.useMemo(() => {
    if (!data) {
      return [];
    }

    const sortedList = [...data].sort((a, b) => {
      switch (sortBy) {
        case AGENT_LIST_SORT_BY.NAME:
          return (a.agentName ?? '').localeCompare(b.agentName ?? '');
        case AGENT_LIST_SORT_BY.RECENT:
          return a.startTimestamp - b.startTimestamp;
        case AGENT_LIST_SORT_BY.ID:
        default:
          return a.agentId.localeCompare(b.agentId);
      }
    });

    if (!filterKeyword.trim()) {
      return sortedList;
    }

    const fuse = new Fuse(sortedList, {
      keys: ['agentId'],
      threshold: 0.3,
    });
    return fuse.search(filterKeyword).map(({ item }) => item);
  }, [data, filterKeyword, sortBy]);

  function renderIcon(state: number) {
    if (state === 200 || state === 201) {
      return <FaArrowAltCircleDown color={colors?.error} size={16} />;
    }
    if (state === 300) {
      return <FaTimesCircle color={colors?.error} size={16} />;
    }
    if (state === -1) {
      return <FaExclamationCircle color={colors?.error} size={16} />;
    }

    return <PiHardDriveFill size={16} />;
  }

  return (
    <TooltipProvider>
      <div className={className} style={style}>
        {filteredList && filteredList.length > 0 ? (
          filteredList?.map((instance, i) => {
            return (
              <Tooltip key={instance.agentId}>
                <TooltipTrigger asChild>
                  <div
                    className={cn(
                      'flex items-center gap-2 p-1 px-1 text-xs rounded cursor-pointer hover:bg-accent h-7',
                      {
                        'bg-accent': selectedAgentId === instance.agentId,
                      },
                    )}
                    onClick={() => onClickAgent?.(instance)}
                  >
                    {agentRenderer ? (
                      agentRenderer(instance)
                    ) : (
                      <div className="grid grid-cols-[1rem_auto] items-center">
                        {selectedAgentId === instance.agentId ? <RxCheck /> : <span />}
                        <div className="truncate">
                          <span className="mr-1 align-bottom">
                            {renderIcon(instance?.status?.state?.code)}
                          </span>
                          {instance?.agentName || instance.agentId}
                        </div>
                      </div>
                    )}
                  </div>
                </TooltipTrigger>
                <TooltipContent>
                  <>
                    <div>
                      <span className="text-gray-500">Agent ID:</span> {instance.agentId}
                    </div>
                    <div>
                      <span className="text-gray-500">Agent Name:</span> {instance.agentName}
                    </div>
                  </>
                </TooltipContent>
              </Tooltip>
            );
          })
        ) : (
          <div className="flex justify-center items-center p-3 opacity-50">{emptyMessage}</div>
        )}
      </div>
    </TooltipProvider>
  );
};
