import React from 'react';
import { RxCheck } from 'react-icons/rx';
import { LuChevronRight, LuChevronDown } from 'react-icons/lu';
import { AGENT_LIST_SORT, useGetAgentList } from '@pinpoint-fe/hooks';
import { SearchApplication } from '@pinpoint-fe/constants';
import { cn } from '../../lib/utils';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '../ui/collapsible';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '../ui';

export interface AgentListFetcherProps {
  className?: string;
  sortBy?: AGENT_LIST_SORT;
  isCollapsible?: boolean;
  filterKeyword?: string;
  selectedAgentId?: string;
  emptyMessage?: React.ReactNode;
  agentRenderer?: (agent: SearchApplication.Instance) => React.ReactNode;
  onClickAgent?: (agent: SearchApplication.Instance) => void;
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
}: AgentListFetcherProps) => {
  const { data } = useGetAgentList({ sortBy });
  const [openStates, setOpenStates] = React.useState<boolean[]>([]);
  const filteredList = React.useMemo(() => {
    return data?.reduce((acc, curr) => {
      const filteredInstanceList = curr.instancesList.filter((instance) =>
        new RegExp(filterKeyword, 'i').test(instance.agentId),
      );
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

  return (
    <div className={className}>
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
                    <TooltipTrigger className="w-full">
                      <div className="flex items-center">
                        <div className="truncate">{group.groupName}</div>
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
                            <TooltipTrigger>
                              <div className="grid grid-cols-[1rem_auto] items-center">
                                {selectedAgentId === instance.agentId ? <RxCheck /> : <span />}
                                <div className="truncate">{instance.agentId}</div>
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
