import { useTranslation } from 'react-i18next';
import { ConfigUsers } from '@pinpoint-fe/ui/src/constants';
import { UsersTableToolbar, UsersTableToolbarProps } from './UsersTableToolbar';
import { ColumnDef } from '@tanstack/react-table';
import { DataTable } from '../../../components';
import { cn } from '../../../lib';

export interface UsersTableFetcherProps {
  data?: ConfigUsers.User[];
  hideAddButton?: UsersTableToolbarProps['hideAddButton'];
  enableUserEdit?: UsersTableToolbarProps['enableUserEdit'];
  onClickRow?: (user: ConfigUsers.User) => void;
  onClickAdd?: UsersTableToolbarProps['onClickAdd'];
  onClickSearch?: UsersTableToolbarProps['onClickSearch'];
  actionRenderer?: (user: ConfigUsers.User) => React.ReactNode;
}

export interface UsersTableAction {
  refresh: () => void;
}

export const UsersTable = ({
  data,
  hideAddButton,
  enableUserEdit,
  actionRenderer,
  onClickSearch,
  onClickRow,
  onClickAdd,
}: UsersTableFetcherProps) => {
  const { t } = useTranslation();

  const columns: ColumnDef<ConfigUsers.User>[] = [
    {
      accessorKey: 'name',
      header: t('CONFIGURATION.USERS.LABEL.USER_NAME') || 'Name',
    },
    {
      accessorKey: 'department',
      header: t('CONFIGURATION.USERS.LABEL.USER_DEPARTMENT') || 'Department',
    },
    {
      header: t('CONFIGURATION.COMMON.LABEL.ACTIONS') || 'Actions',
      meta: {
        headerClassName: 'w-20',
        cellClassName: '',
      },
      cell: ({ row }) => {
        return actionRenderer?.(row?.original as ConfigUsers.User);
      },
    },
  ];

  return (
    <div className="space-y-2">
      <UsersTableToolbar
        hideAddButton={hideAddButton}
        enableUserEdit={enableUserEdit}
        onClickSearch={onClickSearch}
        onClickAdd={onClickAdd}
      />
      <div className={cn('border rounded-md')}>
        <DataTable
          autoResize={true}
          columns={columns}
          data={data || []}
          onClickRow={onClickRow ? (rowData) => onClickRow?.(rowData.original) : undefined}
        />
      </div>
    </div>
  );
};
