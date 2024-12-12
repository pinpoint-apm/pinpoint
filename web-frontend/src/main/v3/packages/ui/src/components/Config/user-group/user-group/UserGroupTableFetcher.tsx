import React from 'react';
import { useDeleteConfigUserGroup, useGetConfigUserGroup } from '@pinpoint-fe/ui/hooks';
import { useTranslation } from 'react-i18next';
import { DataTable } from '../../../DataTable';
import { useReactToastifyToast } from '../../../Toast';
import { getUserGroupTableColumns } from './userGroupTableColumns';
import { useNavigate } from 'react-router-dom';
import { APP_PATH, ConfigUserGroup } from '@pinpoint-fe/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/utils';
import { UserGroupTableToolbar } from './UserGroupTableToolbar';

export interface UserGroupTableFetcherProps {
  userId?: string;
  enableUserGroupAdd?: boolean;
  enableAllUserGroupRemove?: boolean;
  enableOnlyMyUserGroupRemove?: boolean;
}

export const UserGroupTableFetcher = ({
  userId = '',
  enableAllUserGroupRemove = false,
  enableOnlyMyUserGroupRemove = true,
  ...props
}: UserGroupTableFetcherProps) => {
  const navigate = useNavigate();
  const toast = useReactToastifyToast();
  const { t } = useTranslation();
  const [query, setQuery] = React.useState('');
  const queryParams = query ? { userGroupId: query } : userId ? { userId } : {};
  const { data, mutate } = useGetConfigUserGroup(queryParams);
  const myUserGroupListRef = React.useRef<ConfigUserGroup.UserGroup[]>();
  const myUserGroupList = query ? myUserGroupListRef.current : data;
  const { isMutating, onRemove } = useDeleteConfigUserGroup({
    onCompleteRemove: () => {
      toast.success(t('COMMON.REMOVE_SUCCESS'), {
        autoClose: 2000,
      });
      mutate();
    },
    onError: () => {
      toast.error(t('COMMON.REMOVE_FAIL'), {
        autoClose: 2000,
      });
    },
  });

  const columns = getUserGroupTableColumns({
    disabled: (userGroupName: string) =>
      !(
        enableAllUserGroupRemove ||
        (enableOnlyMyUserGroupRemove && myUserGroupList?.some(({ id }) => id === userGroupName))
      ) || isMutating,
    label: {
      userGroupName: t('CONFIGURATION.USER_GROUP.LABEL.USER_GROUP_NAME'),
      actions: t('CONFIGURATION.COMMON.LABEL.ACTIONS'),
    },
    onClickRemove: (userGroupName: string) => onRemove({ id: userGroupName, userId }),
  });

  React.useEffect(() => {
    if (query) {
      return;
    }

    myUserGroupListRef.current = data;
  }, [data]);

  return (
    <div className="space-y-2">
      <UserGroupTableToolbar
        userId={userId}
        onCompleteAdd={mutate}
        onClickSearch={setQuery}
        {...props}
      />
      <div className="border rounded-md">
        <DataTable
          autoResize
          columns={columns}
          data={data || []}
          onClickRow={(data) => {
            navigate(
              `${APP_PATH.CONFIG_USER_GROUP}?${convertParamsToQueryString({
                groupName: data.original.id,
              })}`,
            );
          }}
        />
      </div>
    </div>
  );
};
