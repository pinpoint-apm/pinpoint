import React from 'react';
import styled from '@emotion/styled';
import { FaChevronDown, FaRedo } from 'react-icons/fa';

import Dropdown from '../Dropdown/Dropdown';
import { StyleFlexVCentered, StyleSpin } from '../Styled/styles';
import { Item, List } from './ApplicationList';

export interface Application {
  
}

export interface ApplicationSelectorProps {
  children?: React.ReactNode;
  onReload?: () => void;
  onChangeInput?: () => void;
  onSelectApplication?: () => void;
}

const ApplicationSelector = ({
  children,
  onReload,
  onChangeInput,
  onSelectApplication,
}: ApplicationSelectorProps) => {
  const [ isOpen, setOpen ] = React.useState(false);

  function handleChangeDropdown({ open }: { open: boolean }) {
    setOpen(open);
  }

  return (
    <StyledDropdown onChange={handleChangeDropdown} open {...{isOpen}}>
      <StyledTrigger {...{isOpen}}>
        Select your application
        <StyledArrowIcon />
      </StyledTrigger>
      <StyledContent {...{isOpen}}>
        <StyledListWrapper>
          {children}
        </StyledListWrapper>
        <StyledReloadButtonWrapper>
          Reload <StyledReloadButton />
        </StyledReloadButtonWrapper>
      </StyledContent>
    </StyledDropdown>
  );
};

const StyledDropdown = styled(Dropdown)<{ isOpen: boolean; }>`
  width: 310px;
  ${({isOpen}) => {
    return {
      boxShadow: isOpen ? '0 0 1px rgba(0,0,0,.1), 0 1px 2px rgba(0,0,0,.32)' : '',
    }
  }}
`

const StyledTrigger = styled(Dropdown.Trigger)<{ isOpen: boolean; }>`
  ${StyleFlexVCentered}
  height: 32px;
  padding: 0 13px;
  color: var(--text-primary-lightest);
  border: 1px solid var(--blue-700);
  cursor: pointer;
  border-radius: ${({ isOpen }) => {
    return isOpen 
      ? `var(--border-radius) var(--border-radius) 0 0;`
      : `var(--border-radius);`
  }};
  &:hover {
    ${({isOpen}) => {
      return {
        boxShadow: !isOpen ? '0 0 1px rgba(0,0,0,.1), 0 1px 2px rgba(0,0,0,.32)' : '',
      }
    }}
  }
`

const StyledArrowIcon = styled(FaChevronDown)`
  margin-left: auto;
`

const StyledContent = styled(Dropdown.Content)<{ isOpen: boolean; }>`
  border: 1px solid var(--blue-700);
  border-top: 0px;
  border-radius: 0 0 var(--border-radius) var(--border-radius);
`

const StyledListWrapper = styled.div`
  max-height: 418px;
  overflow-y: auto;
`

const StyledReloadButtonWrapper = styled.button`
  height: 30px;
  width: 100%;
  text-align: center;
  color: var(--text-knockout);
  background-color: var(--primary);
`

const StyledReloadButton = styled(FaRedo)<{ spin?: boolean; }>`
  ${({spin}) => spin && StyleSpin}
`

export default Object.assign(ApplicationSelector, {
  Item,
  List,
})
