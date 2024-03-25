import React from 'react';
import { Button, Popover, PopoverContent, PopoverTrigger } from '../../../ui';
import { ConfigGroupMember } from '@pinpoint-fe/constants';
import { UsersTable } from '../../users';
import { MdOutlineAddCircleOutline } from 'react-icons/md';
import { toast } from '../../../Toast';
import { useTranslation } from 'react-i18next';
import { usePostConfigGroupMember } from '@pinpoint-fe/hooks';

export interface GroupMemberAddPopupProps {
  popupTrigger: React.ReactNode;
  onCompleteAdd?: () => void;
  userGroupId: string;
  userDepartment?: string;
  groupMember?: ConfigGroupMember.GroupMember[];
}

export const GroupMemberAddPopup = ({
  popupTrigger,
  onCompleteAdd,
  userGroupId,
  userDepartment = '',
  groupMember,
}: GroupMemberAddPopupProps) => {
  const { t } = useTranslation();
  const [open, setOpen] = React.useState(false);
  const { isMutating, onSubmit } = usePostConfigGroupMember({
    onCompleteSubmit: () => {
      toast.success(t('COMMON.SUBMIT_SUCCESS'), {
        autoClose: 2000,
      });

      onCompleteAdd?.();
    },
    onError: () => {
      toast.error(t('COMMON.SUBMIT_FAIL'), {
        autoClose: 2000,
      });
    },
  });
  const shouldDisable = (userId: string) => {
    return isMutating || groupMember?.some(({ memberId }) => memberId === userId);
  };
  const handleOnAdd = (userId: string) => {
    onSubmit({ memberId: userId, userGroupId });
  };

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>{popupTrigger}</PopoverTrigger>
      <PopoverContent align="start" className="w-[650px]">
        <UsersTable
          autoResize={false}
          department={userDepartment}
          className="overflow-y-auto max-h-80"
          actionRenderer={(user) => {
            return (
              <Button
                variant="ghost"
                className="px-3"
                disabled={shouldDisable(user.userId)}
                onClick={() => handleOnAdd(user.userId)}
              >
                <MdOutlineAddCircleOutline />
              </Button>
            );
          }}
          hideAddButton={true}
        />
      </PopoverContent>
    </Popover>
  );
};
