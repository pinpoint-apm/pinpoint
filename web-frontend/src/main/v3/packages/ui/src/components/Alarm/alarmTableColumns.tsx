import React from 'react';
import { useTranslation } from 'react-i18next';
import { AlarmRule } from '@pinpoint-fe/ui/constants';
import { CellContext, ColumnDef } from '@tanstack/react-table';
import { RxDotsVertical } from 'react-icons/rx';
import { MdOutlineEmail, MdOutlineSms, MdOutlineWebhook } from 'react-icons/md';
import { Button } from '../../components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '../../components/ui/dropdown-menu';
import { AlarmPermissionContext } from '.';

export const AlarmTypes = ({ data }: { data: AlarmRule.AlarmRuleData }) => {
  const { emailSend, smsSend, webhookSend } = data;
  const isNone = !(emailSend || smsSend || webhookSend);

  return (
    <div className="flex items-center gap-3">
      {isNone ? (
        '-'
      ) : (
        <>
          {emailSend && <MdOutlineEmail className="w-4 h-4" />}
          {smsSend && <MdOutlineSms className="w-4 h-4" />}
          {webhookSend && <MdOutlineWebhook className="w-4 h-4" />}
        </>
      )}
    </div>
  );
};

export interface AlarmTableColumns {
  onClickEdit?: (data?: AlarmRule.AlarmRuleData) => void;
  onClickDelete?: (data?: AlarmRule.AlarmRuleData) => void;
}

export const alarmTableColumns: (
  props: AlarmTableColumns,
) => ColumnDef<AlarmRule.AlarmRuleData>[] = ({ onClickEdit, onClickDelete }) => [
  {
    accessorKey: 'checkerName',
    header: () => 'Rule Name',
    cell: (props) => props.getValue(),
  },

  {
    accessorKey: 'userGroupId',
    header: () => 'User Group',
    cell: (props) => props.getValue(),
    meta: {
      headerClassName: 'w-40',
      cellClassName: '',
    },
  },
  {
    accessorKey: 'threshold',
    header: () => 'Threshold',
    cell: (props) => props.getValue(),
    meta: {
      headerClassName: 'w-36 px-4',
      cellClassName: 'text-right px-4',
    },
  },
  {
    accessorKey: 'type',
    header: () => 'Type',
    cell: (props) => {
      return <AlarmTypes data={props.row.original} />;
    },
    meta: {
      headerClassName: 'w-36 px-4',
      cellClassName: 'px-4',
    },
  },
  {
    accessorKey: 'actions',
    header: () => 'Actions',
    cell: (props) => {
      return <ActionButtons cellProps={props} columnProps={{ onClickEdit, onClickDelete }} />;
    },
    meta: {
      headerClassName: 'w-20',
      cellClassName: 'text-center px-4',
    },
  },
];

const ActionButtons = ({
  cellProps,
  columnProps,
}: {
  cellProps: CellContext<AlarmRule.AlarmRuleData, unknown>;
  columnProps: AlarmTableColumns;
}) => {
  const { t } = useTranslation();
  const { permissionContext } = React.useContext(AlarmPermissionContext);

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="outline" className="px-2 py-1">
          <RxDotsVertical />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent>
        <DropdownMenuItem
          className="hover:cursor-pointer"
          disabled={!permissionContext.edit}
          onClick={(e) => {
            e.stopPropagation();
            columnProps.onClickEdit?.(cellProps.row.original);
          }}
        >
          {t('COMMON.EDIT')}
        </DropdownMenuItem>
        <DropdownMenuSeparator />
        <DropdownMenuItem
          className="text-destructive focus:text-destructive hover:cursor-pointer"
          disabled={!permissionContext.delete}
          onClick={(e) => {
            e.stopPropagation();
            columnProps.onClickDelete?.(cellProps.row.original);
          }}
        >
          {t('COMMON.DELETE')}
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
};
