import React from 'react';
import { Button, Popover, PopoverContent, PopoverTrigger } from '../../../ui';
import { ConfigGroupMember, ConfigUsers } from '@pinpoint-fe/constants';
import { MdOutlineAddCircleOutline } from 'react-icons/md';
import { useReactToastifyToast } from '../../../Toast';
import { useTranslation } from 'react-i18next';
import { useGetConfigUsers, usePostConfigGroupMember } from '@pinpoint-fe/ui/hooks';
import { UsersTable } from '../../users/UsersTable';

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
  const toast = useReactToastifyToast();
  const [open, setOpen] = React.useState(false);
  const [query, setQuery] = React.useState(userDepartment);
  const { data } = useGetConfigUsers(query ? { searchKey: query } : undefined);

  React.useEffect(() => {
    setQuery(userDepartment);
  }, [open, userDepartment]);

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

  function actionRenderer(user: ConfigUsers.User) {
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
  }

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>{popupTrigger}</PopoverTrigger>
      <PopoverContent align="start" className="w-[650px]">
        <UsersTable
          data={data || []}
          hideAddButton={true}
          enableUserEdit={false}
          actionRenderer={actionRenderer}
          onClickSearch={setQuery}
        />
      </PopoverContent>
    </Popover>
  );
};
