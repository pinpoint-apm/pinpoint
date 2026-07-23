import React from 'react';
import { useTranslation } from 'react-i18next';
import { AgentManagementList } from '@pinpoint-fe/ui/src/constants';
import { format } from '@pinpoint-fe/ui/src/utils';
import { ColumnDef } from '@tanstack/react-table';
import { Button, DataTable } from '../../../components';
import { cn } from '../../../lib';
import { FaRegTrashCan } from 'react-icons/fa6';
import { AgentManagementRemovePopup } from './AgentManagementRemovePopup';

export interface AgentManagementTableProps {
  data?: AgentManagementList.Instance[];
  onRemove?: (instance: AgentManagementList.Instance, password?: string) => void;
}

export const AgentManagementTable = ({ data, onRemove }: AgentManagementTableProps) => {
  const { t } = useTranslation();

  // https://github.com/shadcn-ui/ui/issues/4261#issuecomment-2295547143
  const columns: ColumnDef<AgentManagementList.Instance>[] = React.useMemo(
    () => [
      {
        accessorKey: 'agentId',
        header: 'Agent Id',
      },
      {
        accessorKey: 'agentStartTime',
        header: 'Agent Start Time',
        cell: ({ row }) => format(row?.original?.agentStartTime),
      },
      {
        accessorKey: 'agentName',
        header: 'Agent Name',
      },
      {
        accessorKey: 'state',
        header: 'State',
        cell: ({ row }) => {
          const { state, lastStateUpdateTimestamp } = row?.original || {};

          return (
            <div className="flex flex-col">
              <span>{state?.desc}</span>
              <span className="text-xs text-muted-foreground">
                {t('CONFIGURATION.AGENT_MANAGEMENT.LABEL.LAST_UPDATED', {
                  time: format(lastStateUpdateTimestamp),
                })}
              </span>
            </div>
          );
        },
      },
      {
        header: 'Action',
        meta: {
          headerClassName: 'w-20',
          cellClassName: '',
        },
        cell: ({ row }) => {
          const instance = row?.original;

          return (
            <AgentManagementRemovePopup
              popupTrigger={
                <Button
                  variant="ghost"
                  className="px-3"
                  // disabled={!enableUserEdit}
                  onClick={(e) => {
                    e.stopPropagation();
                  }}
                >
                  <FaRegTrashCan />
                </Button>
              }
              agent={instance}
              onClickRemove={(_agent, password) => onRemove?.(instance, password)}
            />
          );
        },
      },
    ],
    [t],
  );

  return (
    <div className={cn('rounded-md border')}>
      <DataTable autoResize={true} columns={columns} data={data || []} />
    </div>
  );
};
