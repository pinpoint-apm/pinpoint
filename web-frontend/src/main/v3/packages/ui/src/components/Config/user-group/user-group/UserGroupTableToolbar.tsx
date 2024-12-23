import React from 'react';
import { useTranslation } from 'react-i18next';
import { RxMagnifyingGlass } from 'react-icons/rx';
import { UserGroupAddPopup } from './UserGroupAddPopup';
import { Button, Input } from '../../../ui';
import { MdOutlineAdd } from 'react-icons/md';
import { useNavigate } from 'react-router-dom';
import { APP_PATH } from '@pinpoint-fe/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/utils';

export interface UserGroupTableToolbarProps {
  onCompleteAdd?: () => void;
  onClickSearch?: (query: string) => void;
  enableUserGroupAdd?: boolean;
  userId: string;
}

export const UserGroupTableToolbar = ({
  onCompleteAdd,
  onClickSearch,
  enableUserGroupAdd = false,
  ...props
}: UserGroupTableToolbarProps) => {
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [inputValue, setInputValue] = React.useState('');

  const handleOnAdd = (userGroupName: string) => {
    navigate(
      `${APP_PATH.CONFIG_USER_GROUP}?${convertParamsToQueryString({ groupName: userGroupName })}`,
    );
    onCompleteAdd?.();
  };

  const handleOnChange = ({ currentTarget }: React.ChangeEvent<HTMLInputElement>) => {
    setInputValue(currentTarget.value);
  };

  const handleKeyDown = ({ key, currentTarget }: React.KeyboardEvent<HTMLInputElement>) => {
    switch (key) {
      case 'Enter':
        onClickSearch?.(currentTarget.value);
        return;
      case 'Escape':
        setInputValue('');
        return;
      default:
        return;
    }
  };

  const handleOnClickSearch = () => {
    onClickSearch?.(inputValue);
  };

  return (
    <div className="flex justify-between">
      <UserGroupAddPopup
        popupTrigger={
          <Button disabled={!enableUserGroupAdd}>
            <MdOutlineAdd className="mr-0.5" />{' '}
            {t('CONFIGURATION.USER_GROUP.LABEL.USER_GROUP_ADD_BUTTON')}
          </Button>
        }
        onCompleteAdd={handleOnAdd}
        {...props}
      />
      <div className="flex items-center w-64 pl-3 pr-2 border rounded shadow-sm h-9">
        <Input
          value={inputValue}
          className="h-full px-0 py-3 border-none focus-visible:ring-0"
          placeholder={t('CONFIGURATION.USER_GROUP.USER_GROUP_SEARCH_PLACEHOLDER')}
          onChange={handleOnChange}
          onKeyDown={handleKeyDown}
        />
        <Button variant="ghost" className="h-full p-0.5 opacity-50" onClick={handleOnClickSearch}>
          <RxMagnifyingGlass />
        </Button>
      </div>
    </div>
  );
};
