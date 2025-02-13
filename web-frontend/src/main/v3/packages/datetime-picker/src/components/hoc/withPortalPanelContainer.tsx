import React from 'react';
import { createPortal } from 'react-dom';
import { DatePanelProps } from '../DatePanel';
import { useOnClickOutside } from 'usehooks-ts';
import { throttle } from '../../utils/functions';

interface WithPortalPanelContainerProps extends DatePanelProps {
  triggerRef: React.RefObject<HTMLDivElement>;
  onClickOutside: () => void;
  getPanelContainer?: () => HTMLElement | null;
}

export const withPortalPanelContainer = (WrappedComponent: React.ComponentType<DatePanelProps>) => {
  return ({
    triggerRef,
    onClickOutside,
    getPanelContainer,
    ...props
  }: WithPortalPanelContainerProps) => {
    const panelWrapperRef = React.useRef<HTMLDivElement>(null);
    const [datePanelStyle, setDatePanelStyle] = React.useState<React.CSSProperties>();

    useOnClickOutside(panelWrapperRef, (event) => {
      const clickedElement = event.target as HTMLElement;
      const parentElement = clickedElement.parentNode as HTMLElement;

      if (!parentElement?.classList?.contains('rich-datetime-picker__trigger')) {
        onClickOutside?.();
      }
    });

    const setPanelStyle = () => {
      const triggerRect = triggerRef.current?.getBoundingClientRect();

      setDatePanelStyle({
        width: triggerRect?.width,
        top: (triggerRect?.bottom || 0) + 2,
        left: triggerRect?.left,
      });
    };

    React.useEffect(() => {
      if (props.open && getPanelContainer && getPanelContainer()) {
        setPanelStyle();
        window.addEventListener('resize', throttle(setPanelStyle, 200));
      } else {
        window.removeEventListener('resize', setPanelStyle);
      }

      return () => {
        window.removeEventListener('resize', setPanelStyle);
      };
    }, [props.open]);

    return getPanelContainer?.() ? (
      createPortal(
        <div className="rich-datetime-picker overflow-hidden" ref={panelWrapperRef}>
          <WrappedComponent style={datePanelStyle} {...props} />
        </div>,
        getPanelContainer() as HTMLElement,
      )
    ) : (
      <WrappedComponent {...props} />
    );
  };
};
