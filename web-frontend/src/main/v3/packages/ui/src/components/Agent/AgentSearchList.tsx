import { AgentList, AgentListProps } from '.';
import { VirtualSearchList } from '../VirtualList';
import { cn } from '../../lib/utils';
import { useAgentListSortBy } from '@pinpoint-fe/hooks';
import { AgentListSortBySelector } from './AgentListSortBySelector';

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

  return (
    <div className={cn('p-5 pb-6 text-sm h-full', className)}>
      <div className="flex items-center mb-3">
        <span className="font-semibold">Agent List</span>
        <AgentListSortBySelector
          align="end"
          triggerClassName="w-auto h-8 px-2 py-1 ml-auto text-xs border-none shadow-none hover:bg-accent hover:text-accent-foreground justify-start"
        />
      </div>
      <VirtualSearchList
        className="h-full max-h-[calc(100%-2.75rem)] [&>*:first-child]:border [&>*:first-child]:rounded-t"
        inputClassName="focus-visible:ring-0 border-none shadow-none"
        placeHolder="Input agent name"
      >
        {(props) => {
          return (
            <>
              <AgentList
                className="p-2 max-h-[calc(100%-2.25rem)] overflow-y-auto border border-t-0 rounded-b"
                sortBy={sortBy}
                filterKeyword={props.filterKeyword}
                selectedAgentId={selectedAgentId}
                onClickAgent={(agent) => onClickAgent?.(agent)}
              />
            </>
          );
        }}
      </VirtualSearchList>
    </div>
  );
};
