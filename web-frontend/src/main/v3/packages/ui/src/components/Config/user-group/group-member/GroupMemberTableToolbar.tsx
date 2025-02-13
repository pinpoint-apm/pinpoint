import React from 'react';
import { useTranslation } from 'react-i18next';
import { RxMagnifyingGlass } from 'react-icons/rx';
import { GroupMemberAddPopup } from './GroupMemberAddPopup';
import { Button, Input } from '../../../ui';
import { MdOutlineAdd } from 'react-icons/md';
import { ConfigGroupMember } from '@pinpoint-fe/ui/src/constants';

export interface GroupMemberTableToolbarProps {
  onCompleteAdd?: () => void;
  onClickSearch?: (query: string) => void;
  userGroupId: string;
  userDepartment?: string;
  groupMember?: ConfigGroupMember.GroupMember[];
  enableGroupMemberAdd?: boolean;
}

export const GroupMemberTableToolbar = ({
  onCompleteAdd,
  onClickSearch,
  enableGroupMemberAdd = false,
  ...props
}: GroupMemberTableToolbarProps) => {
  const { t } = useTranslation();
  const [inputValue, setInputValue] = React.useState('');

  const handleOnAdd = () => {
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
      <div className="flex items-center w-64 pl-3 pr-2 border rounded shadow-sm h-9">
        <Input
          value={inputValue}
          className="h-full px-0 py-3 border-none focus-visible:ring-0"
          placeholder={t('CONFIGURATION.USER_GROUP.GROUP_MEMBER_SEARCH_PLACEHOLDER')}
          onChange={handleOnChange}
          onKeyDown={handleKeyDown}
        />
        <Button variant="ghost" className="h-full p-0.5 opacity-50" onClick={handleOnClickSearch}>
          <RxMagnifyingGlass />
        </Button>
      </div>
      <GroupMemberAddPopup
        popupTrigger={
          <Button disabled={!enableGroupMemberAdd}>
            <MdOutlineAdd className="mr-0.5" />{' '}
            {t('CONFIGURATION.USER_GROUP.LABEL.GROUP_MEMBER_ADD_BUTTON')}
          </Button>
        }
        onCompleteAdd={handleOnAdd}
        {...props}
      />
    </div>
  );
};
