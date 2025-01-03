import React from 'react';
import { useCaptureKeydown } from '@pinpoint-fe/ui/hooks';
import { Manager, Reference, Popper as PP, PopperProps as PPProps } from 'react-popper';
import { useOnClickOutside } from 'usehooks-ts';
import { cn } from '../../lib';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export interface PopperProps extends Omit<PPProps<any>, 'children'> {
  content: React.ReactNode;
  children: React.ReactElement;
  className?: string;
  positionUpdatable?: boolean;
  hideArrow?: boolean;
  renderArrow?: () => React.ReactNode;
  shouldAlwaysShow?: boolean;
}

export const POPPER_ROOT = '__pinpoint_popper_root__';
export const POPPER_ID = '__pinpoint_popper__';

export const Popper = ({
  children,
  content,
  className,
  renderArrow,
  hideArrow,
  positionUpdatable,
  placement = 'bottom-start',
  shouldAlwaysShow = false,
  modifiers = [],
  ...props
}: PopperProps) => {
  const referenceRef = React.useRef<HTMLDivElement>(null);
  const popperRef = React.useRef(null);
  const [open, setOpen] = React.useState(shouldAlwaysShow);

  useCaptureKeydown((event: KeyboardEvent) => {
    if (event.code === 'Escape' && !shouldAlwaysShow) {
      open && setOpen(false);
    }
  });

  useOnClickOutside(referenceRef, () => {
    if (!shouldAlwaysShow) setOpen(false);
  });

  const handleClickTarget = () => {
    if (!shouldAlwaysShow) setOpen(!open);
  };

  // const handleMouseEnter = () => {
  //   // popperElement?.setAttribute('data-show', 'true');
  //   // arrowElement?.setAttribute('data-show', 'true');
  // };

  // const handleMouseLeave = () => {
  //   // if (!fixed) {
  //   // popperElement?.removeAttribute('data-show');
  //   // arrowElement?.removeAttribute('data-show');
  //   // }
  // };

  return (
    <Manager>
      <Reference>
        {({ ref }) =>
          React.cloneElement(children, {
            ref: mergeRefs([ref, referenceRef]),
            onClick: handleClickTarget,
          })
        }
      </Reference>
      {open && (
        <PP
          modifiers={[
            {
              name: 'offset',
              options: {
                offset: [0, 16], // [x, y]
              },
            },
            {
              name: 'arrow',
              options: {
                padding: 5,
                element: popperRef.current,
              },
            },
            ...modifiers,
          ]}
          placement={placement}
          {...props}
        >
          {({ ref, style, placement, arrowProps }) => {
            const popperStyle = { ...style };

            if (positionUpdatable) {
              if (referenceRef.current?.style) {
                Object.assign(popperStyle, {
                  top: referenceRef.current?.style?.top,
                  left: referenceRef.current?.style?.left,
                  transform: `${
                    referenceRef.current?.style.transform
                  } translateY(${getYFromTransform3d(style.transform)})`,
                });
              } else {
                const offsetOption = modifiers.find((m) => m.name === 'offset')?.options;

                if (offsetOption) {
                  const offset = (offsetOption as { offset: [number, number] })?.offset;

                  offset &&
                    Object.assign(popperStyle, {
                      top: offset?.[1],
                      left: offset?.[0],
                      transform: '',
                    });
                }
              }
            }

            return (
              <div
                ref={mergeRefs([ref, popperRef])}
                style={popperStyle}
                className={cn('bg-white z-[999] rounded', className)}
                data-placement={placement}
              >
                <>
                  {content}
                  {!hideArrow &&
                    (renderArrow ? (
                      renderArrow()
                    ) : (
                      <div
                        className="absolute w-4 h-4 rotate-45 bg-inherit tarnsform -top-2 left-[12%]"
                        ref={arrowProps.ref}
                      />
                    ))}
                </>
              </div>
            );
          }}
        </PP>
      )}
    </Manager>
  );
};

const mergeRefs = <T,>(
  refs: Array<React.MutableRefObject<T> | React.LegacyRef<T>>,
): React.RefCallback<T> => {
  return (value) => {
    refs.forEach((ref) => {
      if (typeof ref === 'function') {
        ref(value);
      } else if (ref != null) {
        (ref as React.MutableRefObject<T | null>).current = value;
      }
    });
  };
};

const getYFromTransform3d = (transformString = '') => {
  const match = transformString.match(/translate3d\(.*?,\s*([^,\s]+)/);
  const extractedValue = match ? match[1] : '';

  return extractedValue;
};
