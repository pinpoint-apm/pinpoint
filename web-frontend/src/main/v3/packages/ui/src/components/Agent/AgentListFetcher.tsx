import React from 'react';
import Fuse from 'fuse.js';
import { RxCheck } from 'react-icons/rx';
import { LuChevronRight, LuChevronDown } from 'react-icons/lu';
import { AGENT_LIST_SORT, useGetAgentList } from '@pinpoint-fe/ui/src/hooks';
import { SearchApplication, colors } from '@pinpoint-fe/ui/src/constants';
import { cn } from '../../lib/utils';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '../ui/collapsible';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '../ui';
import { FaArrowAltCircleDown, FaExclamationCircle, FaTimesCircle } from 'react-icons/fa';
import { BiSolidServer } from 'react-icons/bi';
import { PiHardDriveFill } from 'react-icons/pi';

export interface AgentListFetcherProps extends Pick<React.HTMLAttributes<HTMLDivElement>, 'style'> {
  className?: string;
  sortBy?: AGENT_LIST_SORT;
  isCollapsible?: boolean;
  filterKeyword?: string;
  selectedAgentId?: string;
  emptyMessage?: React.ReactNode;
  agentRenderer?: (agent: SearchApplication.Instance) => React.ReactNode;
  onClickAgent?: (agent?: SearchApplication.Instance) => void;
}

export const AgentListFetcher = ({
  className,
  sortBy,
  isCollapsible = true,
  selectedAgentId,
  filterKeyword = '',
  emptyMessage = 'No Agents',
  agentRenderer,
  onClickAgent,
  style,
}: AgentListFetcherProps) => {
  const { data } = useGetAgentList({ sortBy });
  const [openStates, setOpenStates] = React.useState<boolean[]>([]);
  const filteredList = React.useMemo(() => {
    return data?.reduce((acc, curr) => {
      const fuzzySearch = new Fuse(curr.instancesList, {
        keys: ['agentId'],
        threshold: 0.3,
      });
      const filteredInstanceList = filterKeyword
        ? fuzzySearch.search(filterKeyword).map(({ item }) => item)
        : curr.instancesList;
      if (filteredInstanceList.length > 0) {
        acc.push({
          ...curr,
          instancesList: filteredInstanceList,
        });
      }
      return acc;
    }, [] as SearchApplication.Response);
  }, [data, filterKeyword]);

  React.useEffect(() => {
    if (isCollapsible) {
      const opens = filteredList?.map(() => true);

      setOpenStates(opens || []);
    }
  }, [filteredList, isCollapsible]);

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
    <div className={className} style={style}>
      {filteredList && filteredList.length > 0 ? (
        filteredList?.map((group, i) => {
          const isOpen = isCollapsible ? openStates[i] : true;

          return (
            <Collapsible
              key={i}
              open={isOpen}
              onOpenChange={(changedState) => {
                if (isCollapsible) {
                  setOpenStates((prev) =>
                    prev.map((o, j) => {
                      if (j === i) {
                        return changedState;
                      }
                      return o;
                    }),
                  );
                }
              }}
            >
              <TooltipProvider>
                <CollapsibleTrigger
                  className={cn({
                    'text-xs gap-4 p-2 py-3 rounded disabled:opacity-50 cursor-pointer hover:bg-accent w-full':
                      isCollapsible,
                  })}
                >
                  <Tooltip>
                    <TooltipTrigger className="w-full" asChild>
                      <div className="flex items-center">
                        <div className="truncate">
                          <BiSolidServer size={16} className="mr-1 align-middle" />
                          {group.groupName}
                        </div>
                        {isCollapsible && (
                          <div className="ml-auto">
                            {isOpen ? <LuChevronDown /> : <LuChevronRight />}
                          </div>
                        )}
                      </div>
                    </TooltipTrigger>
                    <TooltipContent>{group.groupName}</TooltipContent>
                  </Tooltip>
                </CollapsibleTrigger>
                <CollapsibleContent className="">
                  {group.instancesList.map((instance) => {
                    return (
                      <div
                        key={instance.agentId}
                        className={cn(
                          'flex items-center gap-2 p-1 px-2 text-xs rounded cursor-pointer hover:bg-accent h-7',
                          {
                            'bg-accent': selectedAgentId === instance.agentId,
                          },
                        )}
                        onClick={() => onClickAgent?.(instance)}
                      >
                        {agentRenderer ? (
                          agentRenderer(instance)
                        ) : (
                          <Tooltip>
                            <TooltipTrigger asChild>
                              <div className="grid grid-cols-[1rem_auto] items-center">
                                {selectedAgentId === instance.agentId ? <RxCheck /> : <span />}
                                <div className="truncate">
                                  <span className="mr-1 align-bottom">
                                    {renderIcon(instance?.status?.state?.code)}
                                  </span>
                                  {instance.agentId}
                                </div>
                              </div>
                            </TooltipTrigger>
                            <TooltipContent>{instance.agentId}</TooltipContent>
                          </Tooltip>
                        )}
                      </div>
                    );
                  })}
                </CollapsibleContent>
              </TooltipProvider>
            </Collapsible>
          );
        })
      ) : (
        <div className="flex items-center justify-center p-3 opacity-50">{emptyMessage}</div>
      )}
    </div>
  );
};
