import { ColumnDef } from '@tanstack/react-table';
import { FaRegTrashCan } from 'react-icons/fa6';
import { ConfigUserGroup } from '@pinpoint-fe/ui/constants';
import { UserGroupRemovePopup } from './UserGroupRemovePopup';
import { Button } from '../../../ui';

interface UserGroupTableColumnsProps {
  disabled?: (groupName: string) => boolean;
  label?: {
    userGroupName?: string;
    actions?: string;
  };
  onClickRemove?: (groupName: string) => void;
}

export const getUserGroupTableColumns = ({
  disabled,
  label,
  onClickRemove,
}: UserGroupTableColumnsProps): ColumnDef<ConfigUserGroup.UserGroup>[] => [
  {
    accessorKey: 'id',
    header: label?.userGroupName || 'Group name',
  },
  {
    header: label?.actions || 'Actions',
    cell: ({ row }) => {
      return (
        <UserGroupRemovePopup
          popupTrigger={
            <Button
              variant="ghost"
              className="px-3"
              disabled={disabled?.(row.original.id)}
              onClick={(e) => {
                e.stopPropagation();
              }}
            >
              <FaRegTrashCan />
            </Button>
          }
          removeGroupName={row.original.id}
          onClickRemove={onClickRemove}
        />
      );
    },
    meta: {
      headerClassName: 'w-20',
      cellClassName: '',
    },
  },
];
