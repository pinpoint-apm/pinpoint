import React from 'react';
import { createPortal } from 'react-dom';
import { DatePanelProps } from '../DatePanel';
import { useOnClickOutside } from 'usehooks-ts';
import { throttle } from '../../utils/functions';

interface WithPortalPanelContainerProps extends DatePanelProps {
  triggerRef: React.RefObject<HTMLDivElement | null>;
  onClickOutside: () => void;
  getPanelContainer?: () => HTMLElement | null;
}

export const withPortalPanelContainer = (WrappedComponent: React.ComponentType<DatePanelProps>) => {
  const PortalPanelContainer = ({
    triggerRef,
    onClickOutside,
    getPanelContainer,
    ...props
  }: WithPortalPanelContainerProps) => {
    const panelWrapperRef = React.useRef<HTMLDivElement>(null);
    const [datePanelStyle, setDatePanelStyle] = React.useState<React.CSSProperties>();

    useOnClickOutside(panelWrapperRef as React.RefObject<HTMLDivElement>, (event) => {
      const clickedElement = event.target as HTMLElement;
      const parentElement = clickedElement.parentNode as HTMLElement;

      if (!parentElement?.classList?.contains('rich-datetime-picker__trigger')) {
        onClickOutside?.();
      }
    });

    const setPanelStyle = React.useCallback(() => {
      const triggerRect = triggerRef.current?.getBoundingClientRect();

      setDatePanelStyle({
        top: (triggerRect?.bottom || 0) + 2,
        left: triggerRect?.left,
      });
    }, [triggerRef]);

    // 안정된 단일 핸들러 — add/remove에 동일 참조를 써야 리스너가 실제로 해제된다
    const handleResize = React.useMemo(() => throttle(setPanelStyle, 200), [setPanelStyle]);

    React.useEffect(() => {
      if (props.open && getPanelContainer?.()) {
        setPanelStyle();
        window.addEventListener('resize', handleResize);
        return () => window.removeEventListener('resize', handleResize);
      }
    }, [props.open, getPanelContainer, handleResize, setPanelStyle]);

    return getPanelContainer?.() ? (
      createPortal(
        <div
          className="rich-datetime-picker rdp:overflow-hidden"
          ref={panelWrapperRef}
          role="dialog"
          aria-modal="false"
          aria-label="Date range picker"
        >
          <WrappedComponent style={datePanelStyle} {...props} />
        </div>,
        getPanelContainer() as HTMLElement,
      )
    ) : (
      <WrappedComponent {...props} />
    );
  };

  PortalPanelContainer.displayName = `withPortalPanelContainer(${
    WrappedComponent.displayName || WrappedComponent.name || 'Component'
  })`;

  return PortalPanelContainer;
};
