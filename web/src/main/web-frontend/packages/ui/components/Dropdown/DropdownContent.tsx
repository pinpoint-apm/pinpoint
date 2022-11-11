import React, { ReactNode } from 'react';
import styled from '@emotion/styled';
import DropdownContext from './DropdownContext';

export interface DropdownContentProps {
  className?: string;
  children?: ReactNode | ReactNode[];
  closeAfterClick?: boolean;
}

export const DropdownContent = React.forwardRef<HTMLDivElement, DropdownContentProps>(({
  className,
  children,
  closeAfterClick = false,
}, ref) => {
  const { open, setOpen } = React.useContext(DropdownContext)

  function handleClick() {
    closeAfterClick && setOpen(false);
  }

  return (
    <StyledContainer 
      ref={ref}
      open={open}
      className={className} 
      onClick={handleClick}
    >
      {children}
    </StyledContainer>
  );
});

const StyledContainer = styled.div<{ open: boolean }>`
  display: ${({ open }) => open ? 'block' : 'none'};
  background-color: var(--background-layer);
`
