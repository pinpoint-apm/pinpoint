import { ColumnDef } from '@tanstack/react-table';
import { ConfigUsers } from '@pinpoint-fe/constants';

interface UsersTableColumnsProps {
  label?: {
    userName?: string;
    userDepartment?: string;
    actions?: string;
  };
  actionRenderer?: (user: ConfigUsers.User) => React.ReactNode;
}

export const getUsersTableColumns = ({
  label,
  actionRenderer,
}: UsersTableColumnsProps): ColumnDef<ConfigUsers.User>[] => [
  {
    accessorKey: 'name',
    header: label?.userName || 'Name',
  },
  {
    accessorKey: 'department',
    header: label?.userDepartment || 'Department',
  },
  {
    header: label?.actions || 'Actions',
    cell: ({ row }) => {
      return actionRenderer?.(row.original);
    },
    meta: {
      headerClassName: 'w-20',
      cellClassName: '',
    },
  },
];
