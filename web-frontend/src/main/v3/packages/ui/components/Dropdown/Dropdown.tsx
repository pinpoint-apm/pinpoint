import React, { useImperativeHandle, useState, useRef, useMemo, useEffect } from 'react';
import styled from '@emotion/styled';

import { useCaptureKeydown, useOutsideClick, useSkipFirstEffect } from '@pinpoint-fe/utils';
import { DropdownContent } from './DropdownContent';
import { DropdownTrigger } from './DropdownTrigger';
import DropdownContext from './DropdownContext';

export interface DropdownRef {
  close: () => void;
}

export interface DropdownProps {
  open?: boolean;
  className?: string;
  children?: React.ReactNode[];
  hoverable?: boolean;
  onChange?: ({ open }: { open: boolean }) => void;
}

const DropdownRoot = React.forwardRef(({
  open: initOpen = false,
  className,
  children,
  hoverable,
  onChange,
}: DropdownProps, ref) => {
  const [open, setOpen] = useState(initOpen);
  const dropdownRef = useRef(null);

  useOutsideClick(dropdownRef, () => {
    open && setOpen(false);
  })

  useCaptureKeydown(event => {
    if (event.code === 'Escape') {
      open && setOpen(false);
    }
  })

  useSkipFirstEffect(() => {
    onChange?.({
      open,
    })
  }, [open, onChange])

  useImperativeHandle(ref, () => ({
    close() {
      closeContent();
    }
  }))

  function closeContent() {
    setOpen(false);
  }

  function handleMouseEnter() {
    hoverable && setOpen(true);
  }

  function handleMouseLeave() {
    hoverable && setOpen(false);
  }

  return (
    <DropdownContext.Provider value={{ open, setOpen: useMemo(() => setOpen, [setOpen]) }}>
      <StyledContainer
        ref={dropdownRef}
        className={className}
        onMouseLeave={handleMouseLeave}
        onMouseEnter={handleMouseEnter}
      >
        {children}
      </StyledContainer>
    </DropdownContext.Provider>
  );
});

const StyledContainer = styled.div`
  position: relative;
  z-index: 1000;
`

export default Object.assign(DropdownRoot, {
  Trigger: DropdownTrigger,
  Content: DropdownContent,
})
