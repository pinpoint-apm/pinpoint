import React, { FC, memo, useContext, ReactNode } from 'react';
import classNames from 'classnames';

import DropdownContext from './DropdownContext';

export interface DropdownTriggerProps {
  className?: string;
  children?: ReactNode;
  disable?: boolean;
  onClick?: () => void;
}

export const DropdownTrigger: FC<DropdownTriggerProps> = memo(({
  children,
  onClick,
  className,
  disable,
}: DropdownTriggerProps) => {
  const { open, setOpen } = useContext(DropdownContext)

  function handleClick() {
    if (!disable) {
      setOpen(!open)
      onClick?.();
    }
  }

  return (
    <div 
      className={classNames('dropdown-trigger', className)} 
      onClick={handleClick}
    >
      {children}
    </div>
  );
});
