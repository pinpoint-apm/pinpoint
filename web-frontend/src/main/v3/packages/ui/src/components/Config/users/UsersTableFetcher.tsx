import React from 'react';
import { useTranslation } from 'react-i18next';
import {
  useGetConfigUsers,
  usePostConfigUser,
  usePutConfigUser,
  useDeleteConfigUser,
} from '@pinpoint-fe/ui/hooks';
import { Configuration, ConfigUsers, ErrorResponse } from '@pinpoint-fe/ui/constants';
import { UsersTable } from './UsersTable';
import { FaRegTrashCan } from 'react-icons/fa6';
import { UserForm } from './UserForm';
import { UsersSheet } from './UsersSheet';
import { UserRemovePopup } from './UserRemovePopup';
import { useReactToastifyToast, Button, ErrorToast } from '../../../components';

export interface UsersTableFetcherProps {
  configuration?: Configuration;
}

export interface UsersTableAction {
  refresh: () => void;
}

export const UsersTableFetcher = ({ configuration }: UsersTableFetcherProps) => {
  const { t } = useTranslation();
  const toast = useReactToastifyToast();
  const enableUserEdit = configuration?.editUserInfo;
  const [query, setQuery] = React.useState('');
  const [open, setOpen] = React.useState(false);
  const [selectedUser, setSelectedUser] = React.useState<ConfigUsers.User>();

  const { data, refetch } = useGetConfigUsers(query ? { searchKey: query } : undefined);

  const handleMutationError = (error: ErrorResponse) => {
    toast.error(<ErrorToast error={error} />, {
      className: 'pointer-events-auto',
      bodyClassName: '!items-start',
      autoClose: false,
    });
  };

  const onSuccess = React.useCallback((message: string) => {
    toast.success(message, {
      autoClose: 2000,
    });
    refetch();
  }, []);

  const { mutate: postMutate } = usePostConfigUser({
    onSuccess: () => onSuccess(t('COMMON.CREATE_SUCCESS')),
    onError: handleMutationError,
  });

  const { mutate: putMutate } = usePutConfigUser({
    onSuccess: () => onSuccess(t('COMMON.UPDATE_SUCCESS')),
    onError: handleMutationError,
  });

  const { mutate: deleteMutate } = useDeleteConfigUser({
    onSuccess: () => onSuccess(t('COMMON.REMOVE_SUCCESS')),
    onError: handleMutationError,
  });

  function handleRemove(userId: string) {
    deleteMutate(userId);
  }

  function handleOnClickAdd() {
    setSelectedUser(undefined);
    setOpen(true);
  }

  function handleSubmit(data: ConfigUsers.User) {
    if (selectedUser) {
      putMutate(data);
      setSelectedUser(undefined);
    } else {
      postMutate(data);
    }
    setOpen(false);
  }

  function handleClickRow(clickedUser: ConfigUsers.User) {
    setOpen(true);
    setSelectedUser(clickedUser);
  }

  function actionRenderer(user: ConfigUsers.User) {
    return (
      <UserRemovePopup
        popupTrigger={
          <Button
            variant="ghost"
            className="px-3"
            disabled={!enableUserEdit}
            onClick={(e) => {
              e.stopPropagation();
            }}
          >
            <FaRegTrashCan />
          </Button>
        }
        removeUser={user}
        onClickRemove={() => handleRemove(user?.userId)}
      />
    );
  }

  return (
    <div className="space-y-2">
      <UsersTable
        data={data || []}
        hideAddButton={false}
        enableUserEdit={enableUserEdit}
        actionRenderer={actionRenderer}
        onClickSearch={setQuery}
        onClickRow={handleClickRow}
        onClickAdd={handleOnClickAdd}
      />
      <UsersSheet open={open} onOpenChange={() => setOpen(false)}>
        <UserForm
          userInfo={selectedUser}
          enableUserEdit={true}
          onSubmit={handleSubmit}
          onClickCancel={() => setOpen(false)}
        />
      </UsersSheet>
    </div>
  );
};
