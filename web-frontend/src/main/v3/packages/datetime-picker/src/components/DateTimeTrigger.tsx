import React from 'react';
import { cn } from '../utils/style';
import ClockIcon from '../assets/clock.svg?react';
import { DateTimeInput } from './DateTimeInput';

interface DateTimeTriggerProps {
  open: boolean;
  disable?: boolean;
  isValidInput: boolean;
  /**
   * 입력을 설명하는 오류 메시지 노드의 id. 그 노드가 실제로 렌더되고 있을 때만 호출부가 채운다
   * (패널이 닫히면 언마운트되므로 끊어진 참조가 남으면 안 된다).
   */
  describedBy?: string;
  triggerClassName?: string;
  inputClassName?: string;
  renderIcon?: (defaultIcon: React.ReactNode) => React.ReactNode;
  dateInput: string;
  displayInput: string | undefined;
  displayInputRef: React.RefObject<HTMLInputElement | null>;
  onOpen: () => void;
  onInputChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  onInputKeyDown: (e: React.KeyboardEvent) => void;
}

export const DateTimeTrigger = ({
  open,
  disable,
  isValidInput,
  describedBy,
  triggerClassName = '',
  inputClassName = '',
  renderIcon,
  dateInput,
  displayInput,
  displayInputRef,
  onOpen,
  onInputChange,
  onInputKeyDown,
}: DateTimeTriggerProps) => {
  const defaultIcon = <ClockIcon />;
  const iconNode = renderIcon ? renderIcon(defaultIcon) : defaultIcon;

  return (
    <div
      className={cn(
        'rich-datetime-picker__trigger',
        {
          'rdp:border-primary': open,
          'rdp:border-stateRed': !isValidInput,
        },
        {
          disable: disable,
        },
        triggerClassName,
      )}
      onClick={onOpen}
    >
      {iconNode && (
        <span className="rich-datetime-picker__trigger-icon" aria-hidden="true">
          {iconNode}
        </span>
      )}
      <DateTimeInput
        value={open ? dateInput : displayInput || dateInput}
        inputClassName={inputClassName}
        isOpen={open}
        displayInputRef={displayInputRef}
        onChange={onInputChange}
        onKeyDown={onInputKeyDown}
        hasInputError={!isValidInput}
        describedBy={describedBy}
        onFocus={disable ? undefined : onOpen}
        onClick={(e) => open && e.stopPropagation()}
      />
    </div>
  );
};
