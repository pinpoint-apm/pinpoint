import React from 'react';
import { useTranslation } from 'react-i18next';
import { useGetConfigUsers } from '@pinpoint-fe/hooks';
import { DataTable } from '../../DataTable';
import { getUsersTableColumns } from './usersTableColumns';
import { cn } from '../../../lib';
import { UsersTableToolbar } from './UsersTableToolbar';
import { Button, Separator, Sheet, SheetContent, SheetHeader, SheetTitle } from '../../ui';
import { ConfigUsers } from '@pinpoint-fe/constants';
import { IoMdClose } from 'react-icons/io';
import { UserForm } from './UserForm';

export interface UsersTableFetcherProps {
  className?: string;
  department?: string;
  actionRenderer?: (user: ConfigUsers.User) => React.ReactNode;
  hideAddButton?: boolean;
  enableUserClick?: boolean;
  enableUserEdit?: boolean;
  autoResize?: boolean;
}

export interface UsersTableAction {
  refresh: () => void;
}

export const UsersTableFetcher = React.forwardRef<UsersTableAction, UsersTableFetcherProps>(
  (
    {
      className,
      department = '',
      actionRenderer,
      enableUserClick,
      autoResize,
      ...props
    }: UsersTableFetcherProps,
    ref,
  ) => {
    const { t } = useTranslation();
    const [query, setQuery] = React.useState(department);
    const [openUserInfo, setOpenUserInfo] = React.useState(false);
    const [selectedUserInfo, setSelectedUserInfo] = React.useState<ConfigUsers.User | undefined>();
    const { data, mutate } = useGetConfigUsers(query ? { searchKey: query } : undefined);
    const columns = getUsersTableColumns({
      label: {
        userName: t('CONFIGURATION.USERS.LABEL.USER_NAME'),
        userDepartment: t('CONFIGURATION.USERS.LABEL.USER_DEPARTMENT'),
        actions: t('CONFIGURATION.COMMON.LABEL.ACTIONS'),
      },
      actionRenderer,
    });

    const handleOnClickAdd = () => {
      setSelectedUserInfo(undefined);
      setOpenUserInfo(true);
    };

    const handleOnCompleteSubmit = () => {
      mutate();
      setOpenUserInfo(false);
    };

    const handleOnClickRow = (userId: string) => {
      setSelectedUserInfo(data?.find((user) => user.userId === userId));
      setOpenUserInfo(true);
    };

    React.useImperativeHandle(ref, () => {
      return {
        refresh: () => {
          mutate();
        },
      };
    }, []);

    return (
      <div className="space-y-2">
        <UsersTableToolbar onClickSearch={setQuery} onClickAdd={handleOnClickAdd} {...props} />
        <div className={cn('border rounded-md', className)}>
          <DataTable
            autoResize={autoResize}
            columns={columns}
            data={data || []}
            onClickRow={
              enableUserClick ? (rowData) => handleOnClickRow(rowData.original.userId) : undefined
            }
          />
        </div>
        <Sheet open={openUserInfo} onOpenChange={setOpenUserInfo}>
          <SheetContent
            className="flex flex-col gap-0 w-[500px] sm:max-w-[500px] z-[5000] px-0 py-4"
            hideClose={true}
          >
            <SheetHeader className="px-5 pb-4 bg-secondary/50">
              <SheetTitle className="flex items-center">
                {selectedUserInfo
                  ? t('CONFIGURATION.USERS.USER_INFO_TITLE')
                  : t('CONFIGURATION.USERS.USER_ADD_TITLE')}
                <Button
                  variant="outline"
                  size="icon"
                  className="ml-auto border-none shadow-none"
                  onClick={() => setOpenUserInfo(false)}
                >
                  <IoMdClose className="w-5 h-5" />
                </Button>
              </SheetTitle>
            </SheetHeader>
            <Separator />
            <UserForm
              userInfo={selectedUserInfo}
              onClickCancel={() => setOpenUserInfo(false)}
              onCompleteSubmit={handleOnCompleteSubmit}
              {...props}
            />
          </SheetContent>
        </Sheet>
      </div>
    );
  },
);
