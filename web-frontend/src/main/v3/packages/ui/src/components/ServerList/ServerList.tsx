import React from 'react';
import { GoDotFill } from 'react-icons/go';
import { FaChartLine } from 'react-icons/fa';
import { GetHistogramStatistics, AgentOverview } from '@pinpoint-fe/ui/src/constants';
import { Button, cn, Tooltip, TooltipContent, TooltipTrigger, TooltipProvider } from '../..';

export interface ServerListProps {
  className?: string;
  selectedId?: string;
  data?: AgentOverview.Response;
  statistics?: GetHistogramStatistics.Response;
  onClick?: (instance: AgentOverview.Instance) => void;
  onClickInspectorLink?: (agentId: string) => void;
  itemRenderer?: (instance: AgentOverview.Instance) => React.ReactNode;
}

export const ServerList = ({
  data,
  statistics,
  className,
  selectedId,
  onClick,
  onClickInspectorLink,
  itemRenderer,
}: ServerListProps) => {
  return (
    <div className={cn('h-full', className)}>
      <div className="p-3 flex gap-2 flex-col h-[calc(100%-45px)] overflow-y-auto">
        <TooltipProvider>
          {data?.map((instance) => {
            const slow = statistics?.agentHistogram?.[instance.agentId]?.Slow;
            const error = statistics?.agentHistogram?.[instance.agentId]?.Error;

            return (
              <Tooltip key={instance.agentId}>
                <TooltipTrigger asChild>
                  <div
                    key={instance.agentId}
                    className={cn(
                      'flex items-center h-7 px-2 cursor-pointer rounded text-xs hover:bg-neutral-200 gap-1',
                      {
                        'font-semibold bg-neutral-200': instance.agentId === selectedId,
                      },
                    )}
                    onClick={() => onClick?.(instance)}
                  >
                    <GoDotFill
                      className={cn('fill-status-success mr-1', {
                        'fill-status-warn': !!(slow && slow > 0),
                        'fill-status-fail': !!(error && error > 0),
                      })}
                    />
                    {itemRenderer ? (
                      itemRenderer(instance)
                    ) : (
                      <div className="flex-1 truncate">
                        {instance?.agentName || instance.agentId}
                      </div>
                    )}
                    {instance?.hasInspector && (
                      <Button
                        className="z-10 p-1 ml-auto h-5 rounded-sm text-xxs"
                        onClick={() => onClickInspectorLink?.(instance.agentId)}
                      >
                        <FaChartLine className="text-white" />
                      </Button>
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
          })}
        </TooltipProvider>
      </div>
    </div>
  );
};
