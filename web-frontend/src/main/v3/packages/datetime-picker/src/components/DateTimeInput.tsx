import React from 'react';
import { cn } from '../utils/style';

interface DateTimeInputProps {
  value: string;
  inputClassName?: string;
  isOpen: boolean;
  displayInputRef?: React.RefObject<HTMLInputElement | null>;
  /** 텍스트 입력이 무효한 상태인지 */
  hasInputError?: boolean;
  /**
   * 오류 메시지 노드의 id. 패널이 닫히면 그 노드가 언마운트되므로 호출부가 undefined를 넘겨
   * 끊어진 참조가 남지 않게 한다.
   */
  describedBy?: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  onKeyDown: (e: React.KeyboardEvent) => void;
  onClick?: (e: React.MouseEvent<HTMLInputElement>) => void;
  onFocus?: () => void;
}

const noop = () => {};

export const DateTimeInput = ({
  value,
  inputClassName = '',
  isOpen,
  displayInputRef,
  hasInputError,
  describedBy,
  onChange,
  onKeyDown,
  onClick,
  onFocus,
}: DateTimeInputProps) => {
  return (
    <input
      ref={isOpen ? undefined : displayInputRef}
      type="text"
      value={value}
      className={cn('rich-datetime-picker__input', inputClassName)}
      readOnly={!isOpen}
      aria-haspopup="dialog"
      aria-expanded={isOpen}
      aria-invalid={hasInputError || undefined}
      aria-describedby={describedBy}
      onClick={onClick}
      onFocus={onFocus}
      onChange={isOpen ? onChange : noop}
      onKeyDown={isOpen ? onKeyDown : undefined}
    />
  );
};
