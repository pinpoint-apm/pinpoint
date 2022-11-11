import React from 'react';
import styled from '@emotion/styled';
import { FaChevronDown, FaRedo } from 'react-icons/fa';

import Dropdown from '../Dropdown/Dropdown';
import { StyleFlexVCentered, StyleSpin } from '../Styled/styles';

export interface ApplicationSelectorProps {
  open?: boolean;
  children?: React.ReactNode;
  onClickReload?: () => void;
  onChangeInput?: ({ input }: { input: string}) => void;
  onSelectApplication?: () => void;
  selectedApplication?: React.ReactNode;
}

const ApplicationSelector = ({
  open = false,
  children,
  onClickReload,
  onChangeInput,
  onSelectApplication,
  selectedApplication,
}: ApplicationSelectorProps) => {
  const inputRef = React.useRef<HTMLInputElement>(null);
  const [ isOpen, setOpen ] = React.useState(open);

  React.useEffect(() => {
    if (isOpen) {
      inputRef.current?.focus();
    }
  }, [ isOpen ])

  function handleChangeDropdown({ open }: { open: boolean }) {
    setOpen(open);
  }

  function handleChangeInput(event: React.ChangeEvent<HTMLInputElement>) {
    onChangeInput?.({ input: event.target.value });
  }

  return (
    <div>
      <StyledDropdown 
        onChange={handleChangeDropdown} 
        open={!selectedApplication} 
        {...{isOpen}}
      >
        <StyledTrigger {...{isOpen}}>
          {selectedApplication || 'Select your application'}
          <StyledArrowIcon />
        </StyledTrigger>
        <StyledContent {...{isOpen, closeAfterClick: true}}>
          <StyledInputWrapper>
            <input
              type={'text'}
              ref={inputRef}
              onChange={handleChangeInput}
              placeholder={'Input application name'}
            />
          </StyledInputWrapper>
          <StyledListWrapper>
            {children}
          </StyledListWrapper>
          <StyledReloadButton onClick={onClickReload}>
            Reload <StyledReloadIcon />
          </StyledReloadButton>
        </StyledContent>
      </StyledDropdown>
    </div>
  );
};

export default ApplicationSelector;

const StyledDropdown = styled(Dropdown)<{ isOpen: boolean; }>`
  width: 310px;
  /* ${({isOpen}) => {
    return {
      boxShadow: isOpen ? '0 0 1px rgba(0,0,0,.1), 0 1px 2px rgba(0,0,0,.32)' : '',
    }
  }} */
`

const StyledTrigger = styled(Dropdown.Trigger)<{ isOpen: boolean; }>`
  ${StyleFlexVCentered}
  gap: 10px;
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
  position: absolute;
  top: 100%;
  width: 100%;
  border: 1px solid var(--blue-700);
  border-top: 0px;
  border-radius: 0 0 var(--border-radius) var(--border-radius);
`

const StyledInputWrapper = styled.div`
  width: 100%;
  height: 32px;
  padding: 0 12px;
  margin: 10px 0 5px;

  input {
    width: 100%;
    height: 100%;
    border-radius: var(--border-radius);
    border: 1px solid var(--border-primary);
    padding: 0 7px;
  }
`

const StyledListWrapper = styled.div`
  overflow-y: auto;
`

const StyledReloadButton = styled.button`
  height: 30px;
  width: 100%;
  text-align: center;
  color: var(--text-knockout);
  background-color: var(--primary);
`

const StyledReloadIcon = styled(FaRedo)<{ spin?: boolean; }>`
  ${({spin}) => spin && StyleSpin}
`

