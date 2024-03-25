import React from 'react';
import { Button, Input } from '../../ui';
import { RxMagnifyingGlass } from 'react-icons/rx';
import { useTranslation } from 'react-i18next';
import { MdOutlineAdd } from 'react-icons/md';

export interface UsersTableToolbarProps {
  hideAddButton?: boolean;
  enableUserEdit?: boolean;
  onClickSearch?: (query: string) => void;
  onClickAdd?: () => void;
}

export const UsersTableToolbar = ({
  hideAddButton,
  enableUserEdit = false,
  onClickSearch,
  onClickAdd,
}: UsersTableToolbarProps) => {
  const { t } = useTranslation();
  const [inputValue, setInputValue] = React.useState('');

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
      {!hideAddButton && (
        <Button onClick={onClickAdd} disabled={!enableUserEdit}>
          <MdOutlineAdd className="mr-0.5" /> {t('CONFIGURATION.USERS.LABEL.USER_ADD_BUTTON')}
        </Button>
      )}
      <div className="flex items-center w-64 pl-3 pr-2 border rounded shadow-sm h-9">
        <Input
          value={inputValue}
          className="h-full px-0 py-3 border-none focus-visible:ring-0"
          placeholder={t('CONFIGURATION.USERS.USER_SEARCH_PLACEHOLDER')}
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
