import { ColumnDef } from '@tanstack/react-table';
import { FaRegTrashCan } from 'react-icons/fa6';
import { ConfigGroupMember } from '@pinpoint-fe/ui/src/constants';
import { GroupMemberRemovePopup } from './GroupMemberRemovePopup';
import { Button } from '../../../ui';

interface GroupMemberTableColumnsProps {
  disabled?: boolean;
  label?: {
    userName?: string;
    userDepartment?: string;
    actions?: string;
  };
  onClickRemove?: (groupName: string) => void;
}

export const getGroupMemberTableColumns = ({
  disabled,
  label,
  onClickRemove,
}: GroupMemberTableColumnsProps): ColumnDef<ConfigGroupMember.GroupMember>[] => [
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
      return (
        <GroupMemberRemovePopup
          popupTrigger={
            <Button variant="ghost" className="px-3" disabled={disabled}>
              <FaRegTrashCan />
            </Button>
          }
          removeMember={row.original}
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
