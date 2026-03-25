import React from 'react';
import Fuse from 'fuse.js';
import { RxCheck } from 'react-icons/rx';
import {
  AGENT_LIST_SORT_BY,
  getDateRange,
  useGetAgentList,
  useSearchParameters,
} from '@pinpoint-fe/ui/src/hooks';
import { colors } from '@pinpoint-fe/ui/src/constants';
import { AgentList } from '@pinpoint-fe/ui/src/constants/types/AgentList';
import { cn } from '../../lib/utils';
import { FaArrowAltCircleDown, FaExclamationCircle, FaTimesCircle } from 'react-icons/fa';
import { PiHardDriveFill } from 'react-icons/pi';
import { AgentIdNameTooltip } from './AgentIdNameTooltip';
import { TooltipProvider } from '../ui';

export interface AgentListFetcherProps extends Pick<React.HTMLAttributes<HTMLDivElement>, 'style'> {
  className?: string;
  sortBy?: AGENT_LIST_SORT_BY;
  filterKeyword?: string;
  selectedAgentId?: string;
  emptyMessage?: React.ReactNode;
  agentRenderer?: (agent: AgentList.Instance) => React.ReactNode;
  onClickAgent?: (agent?: AgentList.Instance) => void;
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

  const { data } = useGetAgentList({
    applicationName: application?.applicationName || '',
    serviceTypeName: application?.serviceType || '',
    from,
    to,
  });

  const filteredList = React.useMemo(() => {
    if (!data) {
      return [];
    }

    const sortedList = [...data].sort((a, b) => {
      switch (sortBy) {
        case AGENT_LIST_SORT_BY.STARTTIME_ASC:
          return a.agentStartTime - b.agentStartTime;
        case AGENT_LIST_SORT_BY.NAME_DESC:
          return (b.agentName ?? '').localeCompare(a.agentName ?? '');
        case AGENT_LIST_SORT_BY.NAME_ASC:
          return (a.agentName ?? '').localeCompare(b.agentName ?? '');
        case AGENT_LIST_SORT_BY.STARTTIME_DESC:
        default:
          return b.agentStartTime - a.agentStartTime;
      }
    });

    if (!filterKeyword.trim()) {
      return sortedList;
    }

    const fuse = new Fuse(sortedList, {
      keys: ['agentName'],
      threshold: 0.3,
    });
    return fuse.search(filterKeyword).map(({ item }) => item);
  }, [data, filterKeyword, sortBy]);

  function renderIcon(code: number) {
    if (code === 200 || code === 201) {
      return <FaArrowAltCircleDown color={colors?.error} size={16} />;
    }
    if (code === 300) {
      return <FaTimesCircle color={colors?.error} size={16} />;
    }
    if (code === -1) {
      return <FaExclamationCircle color={colors?.error} size={16} />;
    }

    return <PiHardDriveFill size={16} />;
  }

  return (
    <TooltipProvider delayDuration={0}>
      <div className={className} style={style}>
        {filteredList && filteredList.length > 0 ? (
          filteredList?.map((instance) => {
            return (
              <AgentIdNameTooltip
                key={instance?.agentId}
                agentId={instance.agentId}
                agentName={instance.agentName}
              >
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
                          {renderIcon(instance?.state?.code)}
                        </span>
                        {instance?.agentName || instance.agentId}
                      </div>
                    </div>
                  )}
                </div>
              </AgentIdNameTooltip>
            );
          })
        ) : (
          <div className="flex items-center justify-center p-3 opacity-50">{emptyMessage}</div>
        )}
      </div>
    </TooltipProvider>
  );
};
