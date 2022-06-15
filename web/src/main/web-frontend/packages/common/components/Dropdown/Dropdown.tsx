import React, { FC, memo, useState, useRef, useImperativeHandle, useMemo, useEffect } from 'react';
import { css } from '@emotion/react';

import { useCaptureKeydown, useOutsideClick } from '@/hooks/interaction';
import { DropdownContent, DropdownContentProps } from './DropdownContent';
import { DropdownTrigger, DropdownTriggerProps } from './DropdownTrigger';
import DropdownContext from './DropdownContext';

export interface DropdownRef {
  close: () => void;
}

export interface DropdownProps {
  className?: string;
  children?: React.ReactNode[];
  onChange?: ({ show }: { show: boolean }) => void;
  ref: React.MutableRefObject<DropdownRef>;
}

const Dropdown = React.forwardRef(({
  className,
  children,
  onChange,
}: DropdownProps, ref) => {
  const [ show, setShow ] = useState(false);
  const dropdownRef = useRef(null);

  useOutsideClick(dropdownRef, () => {
    show && setShow(false);
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
  }, [ show ])

  useImperativeHandle(ref, () => ({
    close() {
      closeContent();
    }
  }))

  function closeContent() {
    setShow(false);
  }

  return (
    <DropdownContext.Provider value={{ show, setShow: useMemo(() => setShow, [ setShow ]) }}>
      <div ref={dropdownRef} className={className} css={css`position: relative;`}>
        {children}
      </div>
    </DropdownContext.Provider>
  );
});

export default Object.assign(Dropdown, {
  Trigger: DropdownTrigger,
  Content: DropdownContent,
})
