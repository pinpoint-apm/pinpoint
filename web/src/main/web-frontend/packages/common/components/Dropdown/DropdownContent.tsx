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
  const { show, setShow } = React.useContext(DropdownContext)

  function handleClick() {
    closeAfterClick && setShow(false);
  }

  return (
    <StyledContainer 
      show={show}
      className={className} 
      onClick={handleClick}
    >
      {children}
    </StyledContainer>
  );
});

const StyledContainer = styled.div<{ show: boolean }>`
  display: ${({ show }) => show ? 'block' : 'none'};
  position: absolute;
  width: 100%;
  top: 100%;
`
