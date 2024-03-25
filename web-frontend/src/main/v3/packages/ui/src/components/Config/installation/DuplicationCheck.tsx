import React from 'react';
import { Button, Input } from '../../ui';
import { RxMagnifyingGlass } from 'react-icons/rx';
import { cn } from '../../../lib';

export const DUPLICATION_CHECK_STATUS = {
  SUCCESS: 'success',
  ERROR: 'error',
} as const;
export type CheckStatus = { status: 'success' } | { status: 'error'; message: string } | null;

export interface DuplicationCheckProps {
  className?: string;
  placeholder?: string;
  guideText?: string;
  checkStatus?: CheckStatus;
  onCheck: (inputValue: string) => void;
}

export const DuplicationCheck = ({
  className,
  placeholder,
  guideText,
  checkStatus,
  onCheck,
}: DuplicationCheckProps) => {
  const [inputValue, setInputValue] = React.useState('');
  const isCheckSuccess = checkStatus?.status === DUPLICATION_CHECK_STATUS.SUCCESS;
  const isCheckError = checkStatus?.status === DUPLICATION_CHECK_STATUS.ERROR;

  const handleOnChange = ({ currentTarget }: React.ChangeEvent<HTMLInputElement>) => {
    setInputValue(currentTarget.value);
  };

  const handleKeyDown = ({ key, currentTarget }: React.KeyboardEvent<HTMLInputElement>) => {
    switch (key) {
      case 'Enter':
        onCheck(currentTarget.value);
        return;
      case 'Escape':
        setInputValue('');
        return;
      default:
        return;
    }
  };

  const handleOnClick = () => {
    onCheck(inputValue);
  };

  return (
    <>
      <div
        className={cn(
          'flex items-center pl-3 pr-2 border rounded shadow-sm w-72 h-9',
          { 'border-red-500': isCheckError },
          { 'border-emerald-400': isCheckSuccess },
          className,
        )}
      >
        <Input
          value={inputValue}
          className="h-full px-0 py-3 border-none focus-visible:ring-0"
          placeholder={placeholder}
          onChange={handleOnChange}
          onKeyDown={handleKeyDown}
        />
        <Button variant="ghost" className="h-full p-0.5 opacity-50" onClick={handleOnClick}>
          <RxMagnifyingGlass />
        </Button>
      </div>
      <p className="text-xs text-muted-foreground">{guideText}</p>
      {isCheckError && <p className="text-xs text-red-500">{checkStatus?.message}</p>}
    </>
  );
};
