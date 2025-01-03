import React from 'react';
import { useDeleteConfigGroupMember, useGetConfigGroupMember } from '@pinpoint-fe/ui/hooks';
import { useTranslation } from 'react-i18next';
import { DataTable, RowFilterInfo } from '../../../DataTable';
import { useReactToastifyToast } from '../../../Toast';
import { getGroupMemberTableColumns } from './groupMemberTableColumns';
import { GroupMemberTableToolbar } from './GroupMemberTableToolbar';

export interface GroupMemberTableFetcherProps {
  userGroupId: string;
  userId?: string;
  userDepartment?: string;
  enableAllGroupMemberAdd?: boolean;
  enableOnlyMyGroupMemberAdd?: boolean;
  enableAllGroupMemberRemove?: boolean;
  enableOnlyMyGroupMemberRemove?: boolean;
}

export const GroupMemberTableFetcher = ({
  userGroupId,
  userId = '',
  enableAllGroupMemberAdd = false,
  enableOnlyMyGroupMemberAdd = true,
  enableAllGroupMemberRemove = false,
  enableOnlyMyGroupMemberRemove = true,
  ...props
}: GroupMemberTableFetcherProps) => {
  const { t } = useTranslation();
  const toast = useReactToastifyToast();
  const { data, mutate } = useGetConfigGroupMember({ userGroupId });
  const isMyGroup = data?.some(({ memberId }) => memberId === userId);
  const enableGroupMemberAdd = enableAllGroupMemberAdd || (enableOnlyMyGroupMemberAdd && isMyGroup);
  const { isMutating, onRemove } = useDeleteConfigGroupMember({
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
  const [rowFilterInfo, setRowFilterInfo] = React.useState<RowFilterInfo>();
  const columns = getGroupMemberTableColumns({
    disabled:
      !(enableAllGroupMemberRemove || (enableOnlyMyGroupMemberRemove && isMyGroup)) || isMutating,
    label: {
      userName: t('CONFIGURATION.USERS.LABEL.USER_NAME'),
      userDepartment: t('CONFIGURATION.USERS.LABEL.USER_DEPARTMENT'),
      actions: t('CONFIGURATION.COMMON.LABEL.ACTIONS'),
    },
    onClickRemove: (memberId: string) => onRemove({ memberId, userGroupId }),
  });

  return (
    <div className="space-y-2">
      <GroupMemberTableToolbar
        userGroupId={userGroupId}
        enableGroupMemberAdd={enableGroupMemberAdd}
        onCompleteAdd={mutate}
        onClickSearch={(query) => setRowFilterInfo({ query })}
        groupMember={data}
        {...props}
      />
      <div className="border rounded-md">
        <DataTable
          autoResize
          tableClassName="text-xs"
          columns={columns}
          data={data || []}
          rowFilterInfo={rowFilterInfo}
        />
      </div>
    </div>
  );
};
