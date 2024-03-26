import React from 'react';
import { Configuration } from '@pinpoint-fe/constants';
import { UsersTable } from './UsersTable';
import { Button } from '../../ui';
import { toast } from '../../Toast';
import { FaRegTrashCan } from 'react-icons/fa6';
import { UsersTableAction } from './UsersTableFetcher';
import { UserRemovePopup } from './UserRemovePopup';
import { useDeleteConfigUsers } from '@pinpoint-fe/hooks';
import { useTranslation } from 'react-i18next';

export interface UsersProps {
  configuration?: Configuration;
}

export const Users = ({ configuration }: UsersProps) => {
  const enableUserEdit = configuration?.editUserInfo;
  const usersTableRef = React.useRef<UsersTableAction>(null);
  const { t } = useTranslation();
  const { isMutating, onRemove } = useDeleteConfigUsers({
    onCompleteRemove: () => {
      toast.success(t('COMMON.REMOVE_SUCCESS'), {
        autoClose: 2000,
      });
      usersTableRef?.current?.refresh();
    },
    onError: () => {
      toast.error(t('COMMON.REMOVE_FAIL'), {
        autoClose: 2000,
      });
    },
  });

  return (
    <div className="space-y-2">
      <UsersTable
        autoResize
        ref={usersTableRef}
        enableUserEdit={enableUserEdit}
        actionRenderer={(user) => {
          return (
            <UserRemovePopup
              popupTrigger={
                <Button
                  variant="ghost"
                  className="px-3"
                  disabled={!enableUserEdit || isMutating}
                  onClick={(e) => {
                    e.stopPropagation();
                  }}
                >
                  <FaRegTrashCan />
                </Button>
              }
              removeUser={user}
              onClickRemove={() => onRemove(user.userId)}
            />
          );
        }}
        enableUserClick={true}
      />
    </div>
  );
};
