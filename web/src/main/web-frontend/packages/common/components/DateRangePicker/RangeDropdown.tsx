import React, { FC, useState, useRef, useContext } from 'react';
import styled from '@emotion/styled';
import Marquee from 'react-fast-marquee';
import { FaCalendarAlt } from 'react-icons/fa';
import { css } from '@emotion/react';
import { format, isValid, parse } from 'date-fns';

import Dropdown, { DropdownRef } from '../Dropdown/Dropdown';
import DateRangeContext, { DateRange } from './DateRangeContext';
import { CSSFlexVHCentered } from '../Styled/containers';
import { DatePicker } from './DatePicker';

interface RangeDropdownProps {

}

const DATE_FORMAT = 'yyyy-MM-dd-HH-mm';
const RANGES = Object.values(DateRange).map(value => value);

export const RangeDropdown = ({
  
}: RangeDropdownProps) => {
  const dropdownRef = useRef<DropdownRef>(null);
  const { range, updateRange, dateState, updateDateState } = useContext(DateRangeContext);
  const [ isTriggered, setTriggered ] = useState(false);
  const [ showCalendar, setShowCalendar ] = useState(false);
  const [ input, setInput ] = useState(getFormatedDate(dateState.from, dateState.to));
  const isRealTime = range === DateRange.REAL_TIME;

  React.useEffect(() => {
    setInput(getFormatedDate(dateState.from, dateState.to));
  }, [ dateState ])

  function getFormatedDate(from: Date, to: Date) {
    return `${format(from, DATE_FORMAT)} ~ ${format(to, DATE_FORMAT)}`
  }

  function renderDiscription(dateRange: DateRange) {
    if (dateRange === DateRange.REAL_TIME) {
      return 'Real time'
    }

    return `Past ${dateRange}`
  }

  function handleChangeDropdown({ show }: { show: boolean }) {
    setTriggered(show);
    setShowCalendar(false);
  }

  function handleClickCalendarTrigger(e: React.MouseEvent) {
    e.stopPropagation();
    setShowCalendar(true);
  }

  function handleEnterInput(e: React.KeyboardEvent) {
    if (e.code === 'Enter') {
      const dates = input.split('~');

      try {
        const from = dates[0].trim();
        const to = dates[1].trim();
        const parsedFrom = parse(from,'yyyy-MM-dd-HH-mm', new Date());
        const parsedTo = parse(to,'yyyy-MM-dd-HH-mm', new Date());
        
        if (isValid(parsedFrom) && isValid(parsedTo)) {
          updateDateState({
            from: parse(from,'yyyy-MM-dd-HH-mm', new Date()),
            to: parse(to,'yyyy-MM-dd-HH-mm', new Date()),
          })
          dropdownRef?.current?.close();
        } else {
          throw new Error();
        }
      } catch(error) {
        alert('정확한 형식이 아닙니다. yyyy-MM-dd-HH-mm ~ yyyy-MM-dd-HH-mm 형태로 입력해 주세요.');
      }
    }
  }

  return (
    <StyledDropdownContainer ref={dropdownRef} onChange={handleChangeDropdown}>
      <StyledTrigger isTriggered={isTriggered}>
      {isRealTime ? (
        <Marquee speed={80}>REAL TIME WORKING</Marquee>
      ) : (
        <>
          <StyledLabel wide={isRealTime}>{range}</StyledLabel>
          <input 
            value={input} 
            onChange={e => setInput(e.target.value)}
            onKeyDown={handleEnterInput}
            placeholder={`${DATE_FORMAT} ~ ${DATE_FORMAT}`}
          />
        </>
      )}
      </StyledTrigger>
      <StyledContent closeAfterClick={!showCalendar}>
        <div css={css`display: ${showCalendar && 'none'};`}>
        {RANGES.map((dateRange, i) => (
          <StyledList 
            key={i}
            onClick={() => updateRange(dateRange)}
          >
            <StyledLabel wide={dateRange === DateRange.REAL_TIME}>{dateRange}</StyledLabel>
            {renderDiscription(dateRange)}
          </StyledList>
        ))}
          <StyledList 
            onClick={handleClickCalendarTrigger}
          >
            <StyledLabel>
              <FaCalendarAlt />
            </StyledLabel>
            Select from Calendar
          </StyledList>
        </div>
        <div css={css`display: ${showCalendar ? 'block' : 'none'};`}>
          <DatePicker />
        </div>
      </StyledContent>
    </StyledDropdownContainer>
  );
};

const StyledDropdownContainer = styled(Dropdown)`
  width: 350px;
  font-size: 0.8em;

  input {
    height: 100%;
    width: 100%;
  }
`

const StyledTrigger = styled(Dropdown.Trigger)<{ isTriggered: boolean; }>`
  cursor: pointer;
  display: flex;
  align-items: center;
  height: 100%;
  border: 1px solid var(--border-primary);
  border-radius: ${({ isTriggered }) => {
    return isTriggered 
      ? `var(--border-radius) var(--border-radius) 0 0;`
      : `var(--border-radius);`
  }};
`

const StyledContent = styled(Dropdown.Content)`
  padding: 3px 0;
  border: 1px solid var(--border-primary);
  border-top: 0px;
  border-radius: 0 0 var(--border-radius) var(--border-radius);
`

const StyledList = styled.div`
  cursor: pointer;
  display: flex;
  align-items: center;
  height: 30px;

  :hover {
    background-color: var(--background-hover-primary);
  }
`

const StyledLabel = styled.label<{ wide?: boolean }>`
  ${CSSFlexVHCentered}
  height: 22px;
  border-radius: var(--border-radius);
  background-color: var(--icon-default-lightest);
  margin: 0 10px;
  width: ${({ wide }) => wide ? '62px' : '42px'};
  cursor: pointer;
`