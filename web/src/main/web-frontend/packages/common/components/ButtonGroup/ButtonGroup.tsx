import React, { useState, ReactNode, FC, memo } from 'react';
import styled from '@emotion/styled';
import classNames from 'classnames';
import { SerializedStyles } from '@emotion/react';

export interface ButtonProps {
  children: ReactNode;
  active?: boolean;
  disableActive?: boolean;
  onClick?: () => void;
}

export interface ButtonGroupContainerProps {
  customStyle?: SerializedStyles;
  initActiveIndex?: number;
  children: React.ReactElement<ButtonProps>[]
}

const Button: FC<ButtonProps> = memo(({
  children,
  active,
  onClick,
}: ButtonProps) => {
  return (
    <StyledButton 
      className={classNames({ active })}
      onClick={onClick}
    >
      {children}
    </StyledButton>
  );
});

const Container: FC<ButtonGroupContainerProps> = memo(({
  customStyle,
  initActiveIndex = 0,
  children,
}: ButtonGroupContainerProps) => {
  const [ activeIndex, setActiveIndex ] = useState(initActiveIndex);
  
  function handleClick(index: number, disableActive?: boolean, clickFn?: () => void) {
    return () => {
      !disableActive && setActiveIndex(index)
      clickFn?.();
    }
  }

  return (
    <StyledContainer customStyle={customStyle}>
    {children && React.Children.map(children, (button, i) => {
      if (React.isValidElement(button)) {
        return React.cloneElement(button, { 
          active: activeIndex === i && !button.props.disableActive,
          onClick: handleClick(i, button.props.disableActive ,button.props.onClick),
        })
      }
      return button;
    })}
    </StyledContainer>
  );
});

const ButtonGroup = {
  Container,
  Button
}

export default ButtonGroup;

const StyledContainer = styled.div<{ customStyle?: SerializedStyles}>`
  display: inline-flex;
  flex-direction: row;
  border: 1px solid var(--border-primary);
  
  ${({ customStyle }) => customStyle}  
`

const StyledButton = styled.button`
  border-right: 1px solid var(--border-primary);

  &:nth-last-of-type(1) {
    border: none;
  }
`
