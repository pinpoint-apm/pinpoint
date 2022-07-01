import React, { FC, memo, ReactNode } from 'react';
import styled from '@emotion/styled';
import DropdownContext from './DropdownContext';

export interface DropdownContentProps {
  className?: string;
  children?: ReactNode | ReactNode[];
  closeAfterClick?: boolean;
}

export const DropdownContent: FC<DropdownContentProps> = memo(({
  className,
  children,
  closeAfterClick = false,
}: DropdownContentProps) => {
  const { open, setOpen } = React.useContext(DropdownContext)

  function handleClick() {
    closeAfterClick && setOpen(false);
  }

  return (
    <StyledContainer 
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
  position: absolute;
  width: 100%;
  top: 100%;
`
