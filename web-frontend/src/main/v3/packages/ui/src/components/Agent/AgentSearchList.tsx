import React from 'react';
import { AgentList, AgentListProps } from '.';
import { VirtualSearchList } from '../VirtualList';
import { cn } from '../../lib/utils';
import { useAgentListSortBy } from '@pinpoint-fe/ui/hooks';
import { AgentListSortBySelector } from './AgentListSortBySelector';
import { Badge } from '../../components/ui/badge';
import { RxCross2 } from 'react-icons/rx';
import { useHeightToBottom } from '@pinpoint-fe/ui/hooks';
import { HelpPopover } from '../../components/HelpPopover';

export interface AgentSearchListProps {
  className?: string;
  selectedAgentId?: AgentListProps['selectedAgentId'];
  onClickAgent?: AgentListProps['onClickAgent'];
}

export const AgentSearchList = ({
  className,
  selectedAgentId,
  onClickAgent,
}: AgentSearchListProps) => {
  const [sortBy] = useAgentListSortBy();
  const listContainerRef = React.useRef(null);
  const height = useHeightToBottom({ ref: listContainerRef });

  return (
    <div className={cn('p-5 text-sm space-y-2', className)}>
      <div className="flex items-center">
        <span className="inline-flex gap-1 font-semibold">
          Agent List
          <HelpPopover helpKey="HELP_VIEWER.INSPECTOR.AGENT_LIST" />
        </span>

        <AgentListSortBySelector
          align="end"
          triggerClassName="w-auto h-8 px-2 py-1 ml-auto text-xs border-none shadow-none hover:bg-accent hover:text-accent-foreground justify-start"
        />
      </div>
      {selectedAgentId && (
        <Badge
          variant={'outline'}
          className="flex items-center justify-between gap-2 py-1 font-semibold truncate cursor-pointer bg-secondary"
          onClick={() => onClickAgent?.(undefined)}
        >
          <div className="truncate">{selectedAgentId}</div>
          <RxCross2 className="flex-none" />
        </Badge>
      )}
      <VirtualSearchList
        className="[&>*:first-child]:border [&>*:first-child]:rounded-t"
        inputClassName="focus-visible:ring-0 border-none shadow-none"
        placeHolder="Input agent name"
      >
        {(props) => {
          return (
            <div ref={listContainerRef}>
              <AgentList
                style={{
                  maxHeight: selectedAgentId ? `calc(${height}px - 2.25rem)` : `${height}px`,
                }}
                className="p-2 overflow-y-auto border border-t-0 rounded-b"
                sortBy={sortBy}
                filterKeyword={props.filterKeyword}
                selectedAgentId={selectedAgentId}
                onClickAgent={(agent) => onClickAgent?.(agent)}
              />
            </div>
          );
        }}
      </VirtualSearchList>
    </div>
  );
};
