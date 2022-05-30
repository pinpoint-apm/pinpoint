import React, { FC, memo, ReactNode } from 'react';
import styled from '@emotion/styled';
import DropdownContext from './DropdownContext';

export interface DropdownContentProps {
  className?: string;
  children?: ReactNode | ReactNode[];
}

export const DropdownContent: FC<DropdownContentProps> = memo(({
  className,
  children,
}: DropdownContentProps) => {
  const { show } = React.useContext(DropdownContext)

  return (
    <StyledContainer className={className} show={show}>
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
