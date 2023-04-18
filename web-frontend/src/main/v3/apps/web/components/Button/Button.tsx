import React, { FC, memo } from 'react';
import styled from '@emotion/styled';
import { css } from '@emotion/react';
// import { sample } from '@/utils/sample';

export interface ButtonProps {

}

export const Button: FC<ButtonProps> = memo(({

}: ButtonProps) => {
  function test() {
    // sample();
  }
  return (
    <>
      <StyledButton>WEB button</StyledButton>
      <div css={css`color: var(--blue-200);`}>buttons from apps/web!!</div>
    </>
  );
});

const StyledButton = styled.button`
  padding: 100px;
`;