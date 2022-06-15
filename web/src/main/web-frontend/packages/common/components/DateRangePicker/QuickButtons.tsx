import React, { FC, useContext } from 'react';
import { css } from '@emotion/react';
import { IoPlayBack, IoPlayForward, IoPlaySkipForward, IoPause, IoPlayCircleOutline } from 'react-icons/io5';
import classNames from 'classnames';

import ButtonGroup from '../ButtonGroup/ButtonGroup';
import DateRangeContext, { DateRange, DateRangeTime } from './DateRangeContext';

export interface QuickButtonsProps {

}

export const QuickButtons: FC<QuickButtonsProps> = ({

}: QuickButtonsProps) => {
  const { range, updateRange, updateDateState, dateState } = useContext(DateRangeContext);
  const isRealTime = range === DateRange.REAL_TIME

  function handleClickPrev() {
    const { from, to } = dateState;
    const interval = DateRangeTime[range];
    
    updateDateState({
      from: new Date(from.getTime() - interval),
      to: new Date(to.getTime() - interval),
    })
  }

  function handleClickNext() {
    const { from, to } = dateState;
    const interval = DateRangeTime[range];

    if (to.getTime() + interval > new Date().getTime()) {
      updateDateState({
        from: new Date(Date.now() - interval),
        to: new Date(),
      })
    } else {
      updateDateState({
        from: new Date(from.getTime() + interval),
        to: new Date(to.getTime() + interval),
      })
    }
  }

  function handleClickPlay() {
    updateRange(isRealTime ? DateRange.FIVE_MINUTES : DateRange.REAL_TIME);
  }

  function handleClickLatest() {
    updateDateState({
      from: new Date(Date.now() - DateRangeTime[range]),
      to: new Date(),
    })
  }

  return (
    <ButtonGroup.Container 
      customStyle={buttonGroupContainerStyle}
    >
      <ButtonGroup.Button 
        className={classNames({ disabled: isRealTime })}
        onClick={handleClickPrev}
      >
        <IoPlayBack />
      </ButtonGroup.Button>
      <ButtonGroup.Button
        className={classNames({ toggle: isRealTime })}
        onClick={handleClickPlay}
      >
      {isRealTime
        ? <IoPause css={css`font-size: 1rem;`} /> 
        : <>REAL <IoPlayCircleOutline css={css`font-size: 1.1rem;`} /> TIME</>
      }
      </ButtonGroup.Button>
      <ButtonGroup.Button
        disabled={Math.abs(dateState.to.getTime() - new Date().getTime()) < 1000} 
        onClick={handleClickNext}
        className={classNames({ disabled: isRealTime })}
      >
        <IoPlayForward />
      </ButtonGroup.Button>
      <ButtonGroup.Button 
        onClick={handleClickLatest}
        className={classNames({ disabled: isRealTime })}
      >
        <IoPlaySkipForward />
      </ButtonGroup.Button>
    </ButtonGroup.Container>
  );
};

const buttonGroupContainerStyle = css`
  height: 30px;
  width: 200px;
  border-radius: var(--border-radius);
  
  .toggle {
    background-color: var(--primary);
  }

  .disabled {
    pointer-events: none;

    svg {
      fill: var(--icon-disable);
    }
  }

  button {
    flex: 1;
  }

  button:nth-of-type(2) {
    flex: 2.5;
    font-size: 0.7rem !important;
    font-weight: bold;
  }
`