import { useCaptureKeydown, useOutsideClick } from '../../hooks/interaction';
import React, { FC, memo, useState, useRef, useMemo, useEffect } from 'react';
import { DropdownContent, DropdownContentProps } from './DropdownContent';
import { DropdownTrigger, DropdownTriggerProps } from './DropdownTrigger';
import DropdownContext from './DropdownContext';
import { css } from '@emotion/react';

export interface DropdownProps {
  className?: string;
  children?: React.ReactNode[];
  onChange?: ({ show }: { show: boolean }) => void;
}


const Dropdown: FC<DropdownProps> = memo(({
  className,
  children,
  onChange,
}: DropdownProps) => {
  const [ show, setShow ] = useState(false);
  const dropdownRef = useRef(null);

  useOutsideClick(dropdownRef, () => {
    setShow(false);
  })

  useCaptureKeydown(event => {
    if(event.code === 'Escape') {
      show && setShow(false);
    }
  })

  useEffect(() => {
    onChange?.({
      show,
    })
  }, [ show, onChange ])

  return (
    <div className={className} ref={dropdownRef} css={css`position: relative;`}>
      <DropdownContext.Provider value={{ show, setShow: useMemo(() => setShow, [ setShow ]) }}>
      {children}
      </DropdownContext.Provider>
    </div>
  );
});

export default Object.assign(Dropdown, {
  Trigger: DropdownTrigger,
  Content: DropdownContent,
})
