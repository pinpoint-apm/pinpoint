import React, { useState, ReactNode, FC } from 'react';
import styled from '@emotion/styled';
import classNames from 'classnames';
import { SerializedStyles } from '@emotion/react';

export interface ButtonProps {
  className?: string;
  children: ReactNode;
  active?: boolean;
  disableActive?: boolean;
  disabled?: boolean;
  onClick?: () => void;
}

export interface ButtonGroupContainerProps {
  customStyle?: SerializedStyles;
  initActiveIndex?: number;
  children: React.ReactElement<ButtonProps>[]
}

const Button: FC<ButtonProps> = ({
  children,
  active,
  onClick,
  className,
  disabled,
}: ButtonProps) => {
  return (
    <StyledButton
      disabled={disabled}
      className={classNames({ active }, className)}
      onClick={onClick}
    >
      {children}
    </StyledButton>
  );
};

const Container: FC<ButtonGroupContainerProps> = ({
  customStyle,
  initActiveIndex = 0,
  children,
}: ButtonGroupContainerProps) => {
  const [ activeIndex, setActiveIndex ] = useState(initActiveIndex);
  
  function handleClick(index: number, disableActive?: boolean, clickFn?: () => void) {
    return () => {
      !disableActive && setActiveIndex(index);
      clickFn?.();
    }
  }

  return (
    <StyledContainer customStyle={customStyle}>
    {children && React.Children.map(children, (button, i) => {
      if (React.isValidElement(button)) {
        return React.cloneElement(button, { 
          active: activeIndex === i && !button.props.disableActive,
          onClick: handleClick(i, button.props.disableActive, button.props.onClick),
        })
      }
      return button;
    })}
    </StyledContainer>
  );
};

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
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid var(--border-primary);

  &:nth-last-of-type(1) {
    border: none;
  }

  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }
`
