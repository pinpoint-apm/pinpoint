import React, { FC, memo, MouseEventHandler, ReactNode } from 'react';
import styled from '@emotion/styled';
import { css } from '@emotion/react';
// import { sample } from '@/utils/sample';

export const enum ButtonType {
  Default, Disable, Primary
}

export interface ButtonProps {
  label: ReactNode;
  style?: Pick<React.CSSProperties, 'width' | 'margin'>;
  styleType?: ButtonType;
  onClick?: () => void; 
}

export const Button: FC<ButtonProps> = memo(({
  label,
  style,
  styleType,
  onClick,
}: ButtonProps) => {
  function handleClick(e: any) {
    e.stopPropagation();
    onClick?.();
  }

  return (
    <StyledButton
      style={style}
      styleType={styleType}
      onClick={handleClick}
    >
      {label}
    </StyledButton>
  );
});

const defaultStyles = css`
  padding: 0 10px;
  height: 30px;
  cursor: pointer;
  border-radius: var(--border-radius);
`;

const disableStyles = css`
  ${defaultStyles}
  cursor: not-allowed;
  pointer-events: none;
  color: var(--text-knockout);
  background-color: var(--background-disable);
`

const primayStyles = css`
  ${defaultStyles}
  color: var(--text-knockout);
  background-color: var(--primary);
`

const StyledButton = styled.button<Pick<ButtonProps, 'style' | 'styleType'>>`
  ${({ style, styleType }) => {
    let metaStyle = defaultStyles;
    if (styleType === ButtonType.Disable) metaStyle = disableStyles;
    if (styleType === ButtonType.Primary) metaStyle = primayStyles;
    
    return {
      ...style,
      ...metaStyle,
    }
  }}
`