import styled from '@emotion/styled';
import React, { FC, memo, useContext, ReactNode } from 'react';
import DropdownContext from './DropdownContext';

export interface DropdownTriggerProps {
  className?: string;
  children?: ReactNode;
  onClick?: () => void;
}

export const DropdownTrigger: FC<DropdownTriggerProps> = memo(({
  children,
  onClick,
  className,
}: DropdownTriggerProps) => {
  const { show, setShow } = useContext(DropdownContext)

  function handleClick() {
    setShow?.(!show)
    onClick?.();
  }

  return (
    <StyledContainer className={className} onClick={handleClick}>
      {children}
    </StyledContainer>
  );
});

const StyledContainer = styled.div`
  width: 100%;
`;
