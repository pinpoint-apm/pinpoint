import React from 'react';
import { GoDotFill } from 'react-icons/go';
import { FaChartLine } from 'react-icons/fa';
import { GetResponseTimeHistogram, SearchApplication } from '@pinpoint-fe/ui/constants';
import { Button, cn } from '../..';

export interface ServerListProps {
  className?: string;
  selectedId?: string;
  data?: SearchApplication.Response;
  statistics?: GetResponseTimeHistogram.Response;
  onClick?: (inscance: SearchApplication.Instance) => void;
  onClickInspectorLink?: (agentId: string) => void;
  groupNameRenderer?: (application: SearchApplication.Application) => React.ReactNode;
  itemRenderer?: (
    application: SearchApplication.Application,
    instance: SearchApplication.Instance,
  ) => React.ReactNode;
}

export const ServerList = ({
  data,
  statistics,
  className,
  selectedId,
  onClick,
  onClickInspectorLink,
  groupNameRenderer,
  itemRenderer,
}: ServerListProps) => {
  return (
    <div className={cn('h-full', className)}>
      <div className="p-3 flex gap-2 flex-col h-[calc(100%-45px)] overflow-y-auto">
        {data?.map((d, i) => {
          return (
            <React.Fragment key={i}>
              <div className="flex items-center h-8 gap-1 text-sm">
                {groupNameRenderer ? (
                  groupNameRenderer(d)
                ) : (
                  <div className="truncate">{d.groupName}</div>
                )}
              </div>
              <ul>
                {d.instancesList.map((instance) => {
                  const slow = statistics?.agentHistogram?.[instance.agentId]?.Slow;
                  const error = statistics?.agentHistogram?.[instance.agentId]?.Error;

                  return (
                    <li
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
                      {itemRenderer
                        ? itemRenderer(d, instance)
                        : instance.agentName || instance.agentId}
                      <Button
                        className="z-10 h-5 p-1 ml-auto rounded-sm text-xxs"
                        onClick={() => onClickInspectorLink?.(instance.agentId)}
                      >
                        <FaChartLine className="text-white" />
                      </Button>
                    </li>
                  );
                })}
              </ul>
            </React.Fragment>
          );
        })}
      </div>
    </div>
  );
};
