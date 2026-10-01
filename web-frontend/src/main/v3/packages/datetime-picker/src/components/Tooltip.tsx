import React from 'react';
import { useOnClickOutside } from 'usehooks-ts';
import { cn } from '../utils/style';

interface TooltipProps {
  content: React.ReactNode;
  openOn?: 'hover' | 'click' | 'both';
  /** 툴팁이 트리거 기준 어느 쪽에 열릴지 */
  placement?: 'bottom-end' | 'right-end' | 'right-start';
  /** content 래퍼에 추가할 클래스 */
  className?: string;
  ariaLabel?: string;
  children: React.ReactElement;
}

const OPEN_DELAY = 500;
const CLOSE_DELAY = 250;

export const Tooltip = ({
  content,
  openOn = 'both',
  placement = 'bottom-end',
  className,
  ariaLabel,
  children,
}: TooltipProps) => {
  const [open, setOpen] = React.useState(false);
  const wrapperRef = React.useRef<HTMLDivElement>(null);
  const openTimer = React.useRef<ReturnType<typeof setTimeout> | undefined>(undefined);
  const closeTimer = React.useRef<ReturnType<typeof setTimeout> | undefined>(undefined);

  const hover = openOn === 'hover' || openOn === 'both';
  const click = openOn === 'click' || openOn === 'both';

  useOnClickOutside(wrapperRef as React.RefObject<HTMLDivElement>, () => setOpen(false));

  React.useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setOpen(false);
    };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [open]);

  React.useEffect(
    () => () => {
      clearTimeout(openTimer.current);
      clearTimeout(closeTimer.current);
    },
    [],
  );

  const cancelOpen = () => clearTimeout(openTimer.current);
  const cancelClose = () => clearTimeout(closeTimer.current);
  // 호버 시 바로 열지 않고 500ms 뒤에 연다 (지나가는 마우스로 열리는 것 방지)
  const scheduleOpen = () => {
    cancelClose();
    cancelOpen();
    openTimer.current = setTimeout(() => setOpen(true), OPEN_DELAY);
  };
  const scheduleClose = () => {
    cancelOpen();
    cancelClose();
    closeTimer.current = setTimeout(() => setOpen(false), CLOSE_DELAY);
  };

  const triggerProps = children.props as {
    onClick?: (e: React.MouseEvent) => void;
    onFocus?: (e: React.FocusEvent) => void;
  };

  const trigger = React.cloneElement(children as React.ReactElement<Record<string, unknown>>, {
    'aria-haspopup': 'dialog',
    'aria-expanded': open,
    onClick: (e: React.MouseEvent) => {
      triggerProps.onClick?.(e);
      if (click) {
        cancelOpen();
        setOpen((v) => !v);
      }
    },
    onFocus: (e: React.FocusEvent) => {
      triggerProps.onFocus?.(e);
      if (hover) {
        cancelOpen();
        setOpen(true);
      }
    },
  });

  return (
    <div
      ref={wrapperRef}
      className="rich-datetime-picker__tooltip-wrapper"
      onMouseEnter={hover ? scheduleOpen : undefined}
      onMouseLeave={hover ? scheduleClose : undefined}
    >
      {trigger}
      {open && (
        <div
          role="dialog"
          aria-label={ariaLabel}
          className={cn(
            'rich-datetime-picker__tooltip',
            `rich-datetime-picker__tooltip--${placement}`,
            className,
          )}
          onMouseEnter={hover ? cancelClose : undefined}
          onMouseLeave={hover ? scheduleClose : undefined}
        >
          {content}
        </div>
      )}
    </div>
  );
};
