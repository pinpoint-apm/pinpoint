import styled from '@emotion/styled';
import { useSkipFirstEffect } from '@pinpoint-fe/utils';
import React, { useEffect } from 'react';
import { Dropdown, DropdownProps } from '../Dropdown';

export type BoundOption = {
  list: number[];
  defaultValue: number;
};

export type ServerMapOption = {
  inbound: number;
  outbound: number;
  bidirectional: boolean;
  wasOnly: boolean;
}

export interface ServerMapOptionDropdownProps {
  inbound?: BoundOption;
  outbound?: BoundOption;
  wasOnly?: boolean;
  bidirectional?: boolean;
}

export const ServerMapOptionDropdown = ({
  inbound,
  outbound,
  wasOnly = false,
  bidirectional = false,
}: ServerMapOptionDropdownProps) => {
  const DEFAULT_BOUND_OPTION_LIST = [1, 2, 3, 4];
  const DEFAULT_VALUE = DEFAULT_BOUND_OPTION_LIST[0];
  const inboundList = inbound?.list || DEFAULT_BOUND_OPTION_LIST;
  const outboundList = outbound?.list || DEFAULT_BOUND_OPTION_LIST;
  const [close, setClose] = React.useState(true);

  const [optionState, setOptionState] = React.useState<ServerMapOption>({
    inbound: inbound?.defaultValue || DEFAULT_VALUE,
    outbound: inbound?.defaultValue || DEFAULT_VALUE,
    wasOnly: wasOnly,
    bidirectional: bidirectional,
  })

  const [tempOptionState, setTempOptionState] = React.useState<ServerMapOption>({
    inbound: inbound?.defaultValue || DEFAULT_VALUE,
    outbound: inbound?.defaultValue || DEFAULT_VALUE,
    wasOnly: wasOnly,
    bidirectional: bidirectional,
  });

  useSkipFirstEffect(() => {
    if (close) {
      setTempOptionState(state => ({
        ...state,
        inbound: optionState.inbound,
        outbound: optionState.outbound,
        wasOnly: optionState.wasOnly,
        bidirectional: optionState.bidirectional,
      }));
    }
  }, [close]);

  const handleCloseDropdown: DropdownProps['onChange'] = ({ open }) => {
    setClose(!open)
  }

  const handleBoundItemClick = (param: { [key in keyof (Pick<ServerMapOption, 'inbound'> | Pick<ServerMapOption, 'outbound'>)]: number }) => {
    setTempOptionState(prevState => ({ ...prevState, ...param }))
  }

  return (
    <Dropdown
      onChange={handleCloseDropdown}
    >
      <Dropdown.Trigger>
        hi
      </Dropdown.Trigger>
      <Dropdown.Content>
        <div>
          {inboundList.map((value, i) => {
            return (
              <StyledBoundOptionWrapper
                key={i}
                highlight={value === tempOptionState.inbound}
                onClick={() => handleBoundItemClick({ inbound: value })}
              >
                {value}
              </StyledBoundOptionWrapper>
            );
          })}
        </div>
        <div>
          {outboundList.map((value, i) => {
            return (
              <StyledBoundOptionWrapper
                key={i}
                highlight={value === tempOptionState.outbound}
                onClick={() => handleBoundItemClick({ outbound: value })}
              >
                {value}
              </StyledBoundOptionWrapper>
            )
          })}
        </div>

        <div>
          <div>cancel</div>
          <div>apply</div>
        </div>
      </Dropdown.Content>
    </Dropdown>
  );
};

const StyledBoundOptionWrapper = styled.div<{ highlight: boolean }>`
  background-color: ${({ highlight }) => {
    return highlight && 'var(--background-focus-primary)'
  }};
  
  &:hover {
    background-color: var(--background-focus-primary);
  }
`