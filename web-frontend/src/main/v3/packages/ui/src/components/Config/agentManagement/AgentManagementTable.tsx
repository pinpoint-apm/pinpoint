import React from 'react';
import { SearchApplication } from '@pinpoint-fe/ui/constants';
import { ColumnDef } from '@tanstack/react-table';
import { Button, DataTable } from '../../../components';
import { cn } from '../../../lib';
import { FaRegTrashCan } from 'react-icons/fa6';
import { AgentManagementRemovePopup } from './AgentManagementRemovePopup';

export interface AgentManagementTableProps {
  data?: SearchApplication.Instance[];
  onRemove?: (instance: SearchApplication.Instance) => void;
}

export const AgentManagementTable = ({ data, onRemove }: AgentManagementTableProps) => {
  // https://github.com/shadcn-ui/ui/issues/4261#issuecomment-2295547143
  const columns: ColumnDef<SearchApplication.Instance>[] = React.useMemo(
    () => [
      {
        accessorKey: 'hostName',
        header: 'Host Name',
      },
      {
        accessorKey: 'agentId',
        header: 'Agent Id',
      },
      {
        accessorKey: 'agentName',
        header: 'Agent Name',
      },
      {
        accessorKey: 'agentVersion',
        header: 'Agent Version',
      },
      {
        accessorKey: 'ip',
        header: 'IP',
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
              onClickRemove={() => onRemove?.(instance)}
            />
          );
        },
      },
    ],
    [],
  );

  return (
    <div className={cn('border rounded-md')}>
      <DataTable autoResize={true} columns={columns} data={data || []} />
    </div>
  );
};
