import { VirtualizedDataTable } from '../DataTable';
import { ColumnDef } from '@tanstack/react-table';
import { cn } from '@pinpoint-fe/ui/src/lib';
import React from 'react';
import { TooltipContent, TooltipProvider, Tooltip, TooltipTrigger, Button } from '../ui';
import * as TooltipPrimitive from '@radix-ui/react-tooltip';
import { HiMiniExclamationCircle } from 'react-icons/hi2';
import { ApplicationType, BASE_PATH, colors } from '@pinpoint-fe/ui/src/constants';
import { RxExternalLink } from 'react-icons/rx';
import { getThreadDumpPath } from '@pinpoint-fe/ui/src/utils';
import { useRequestService, useSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import { AgentIdNameTooltip } from '../Agent/AgentIdNameTooltip';

export type AgentActiveData = {
  server: string;
  '1s': number;
  '3s': number;
  '5s': number;
  slow: number;
  agentName: string;
  message?: string;
};

const SIZE = 50;

export const AgentActiveTable = ({
  application: targetApplication,
  serviceName: targetServiceName,
  loading,
  data,
  clickedActiveThread,
}: {
  /**
   * threadDump를 열 application. 비DEFAULT servicemap 실시간 보기는 경로에 application이 없고
   * 조회 대상이 map에서 고른 노드로 정해지므로, 그 노드를 넘긴다. 없으면 경로의 application이다.
   */
  application?: ApplicationType;
  /** threadDump를 열 service. 다른 service의 노드를 골랐으면 그 노드의 service다. */
  serviceName?: string;
  loading?: boolean;
  data: AgentActiveData[];
  clickedActiveThread?: string;
}) => {
  const { application: pathApplication } = useSearchParameters();
  const requestService = useRequestService();
  const application = targetApplication ?? pathApplication;
  const serviceName = targetServiceName ?? requestService;
  const focusRowId = React.useMemo(() => {
    return data.findIndex((d) => d.server === clickedActiveThread);
  }, [data, clickedActiveThread]);

  const columns: ColumnDef<AgentActiveData>[] = React.useMemo(
    () => [
      {
        accessorKey: 'server',
        header: 'Server',
        size: 230,
        cell: ({ getValue, row }) => {
          const value = getValue<string>() || '';
          const agentName = row?.original?.agentName || '';
          const message = row?.original?.message || '';

          return (
            <TooltipProvider delayDuration={0}>
              <div className="flex items-center gap-1">
                <AgentIdNameTooltip
                  agentId={value}
                  agentName={agentName}
                  usePortal={true}
                  side="left"
                >
                  <div className="flex items-center gap-1">
                    <span>{agentName}</span>
                  </div>
                </AgentIdNameTooltip>
                <Button
                  className="text-muted-foreground p-0 w-4 h-4 mr-1.5"
                  variant="ghost"
                  onClick={() => {
                    window.open(
                      `${BASE_PATH}${getThreadDumpPath(application, undefined, serviceName)}?agentId=${value}`,
                    );
                  }}
                >
                  <RxExternalLink />
                </Button>
                {message && (
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <div>
                        <HiMiniExclamationCircle color={colors.red[500]} size={16} />
                      </div>
                    </TooltipTrigger>
                    <TooltipPrimitive.Portal>
                      <TooltipContent>
                        <p>{message}</p>
                      </TooltipContent>
                    </TooltipPrimitive.Portal>
                  </Tooltip>
                )}
              </div>
            </TooltipProvider>
          );
        },
      },
      {
        accessorKey: 'slow',
        header: 'Slow',
        size: SIZE,
        meta: {
          headerClassName: 'flex justify-end',
          cellClassName: 'text-right',
        },
        cell: ({ getValue }) => {
          const value = getValue<number>() || 0;

          if (value === -1) {
            return '-';
          }

          return (
            <span
              className={cn({
                'text-red-500 font-bold': value > 0,
              })}
            >
              {value}
            </span>
          );
        },
      },
      {
        accessorKey: '5s',
        header: '5s',
        size: SIZE,
        meta: {
          headerClassName: 'flex justify-end',
          cellClassName: 'text-right',
        },
        cell: ({ getValue }) => {
          const value = getValue<number>() || 0;
          if (value === -1) {
            return '-';
          }
          return (
            <span
              className={cn({
                'text-red-500 font-bold': value > 0,
              })}
            >
              {value}
            </span>
          );
        },
      },
      {
        accessorKey: '3s',
        header: '3s',
        size: SIZE,
        meta: {
          headerClassName: 'flex justify-end',
          cellClassName: 'text-right',
        },
        cell: ({ getValue }) => {
          const value = getValue<number>() || 0;
          if (value === -1) {
            return '-';
          }
          return value;
        },
      },
      {
        accessorKey: '1s',
        header: '1s',
        size: SIZE,
        meta: {
          headerClassName: 'flex justify-end',
          cellClassName: 'text-right',
        },
        cell: ({ getValue }) => {
          const value = getValue<number>() || 0;
          if (value === -1) {
            return '-';
          }
          return value;
        },
      },
    ],
    [application?.applicationName, application?.serviceType, serviceName],
  );

  return (
    <div className="block h-[-webkit-fill-available] max-w-[50%] w-auto">
      <VirtualizedDataTable
        loading={loading}
        tableClassName="[&>tbody]:text-xs w-auto"
        columns={columns}
        data={loading ? [] : data || []}
        focusRowIndex={focusRowId}
        rowClassName={(row) => {
          if (row?.id === String(focusRowId)) {
            return 'bg-yellow-200';
          }
          return '';
        }}
      />
    </div>
  );
};
