import React, { FC, memo, useState, useRef, useImperativeHandle, useMemo, useEffect } from 'react';
import { css } from '@emotion/react';

import { useCaptureKeydown, useOutsideClick } from '../../hooks/interaction';
import { DropdownContent, DropdownContentProps } from './DropdownContent';
import { DropdownTrigger, DropdownTriggerProps } from './DropdownTrigger';
import DropdownContext from './DropdownContext';

export interface DropdownRef {
  close: () => void;
}

export interface DropdownProps {
  className?: string;
  children?: React.ReactNode[];
  onChange?: ({ open }: { open: boolean }) => void;
  hoverable?: boolean;
}


const Dropdown: FC<DropdownProps> = ({
  className,
  children,
  onChange,
  hoverable,
}: DropdownProps, ref) => {
  const [ open, setOpen ] = useState(false);
  const dropdownRef = useRef(null);

  useOutsideClick(dropdownRef, () => {
    setOpen(false);
  })

  useCaptureKeydown(event => {
    if(event.code === 'Escape') {
      open && setOpen(false);
    }
  })

  useEffect(() => {
    onChange?.({
      open,
    })
  }, [ open, onChange ])

  function handleMouseEnter() {
    hoverable && setOpen(true);
  }

  function handleMouseLeave() {
    hoverable && setOpen(false);
  }

  return (
    <DropdownContext.Provider value={{ open, setOpen: useMemo(() => setOpen, [ setOpen ]) }}>
      <div 
        ref={dropdownRef} 
        className={className} 
        css={css`position: relative;`}
        onMouseLeave={handleMouseLeave}
        onMouseEnter={handleMouseEnter}
      >
        {children}
      </div>
    </DropdownContext.Provider>
  );
};

export default Object.assign(Dropdown, {
  Trigger: DropdownTrigger,
  Content: DropdownContent,
})
