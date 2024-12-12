import React from 'react';
import { throttle } from 'lodash';

interface UseHeightToBottomProps {
  ref: React.RefObject<HTMLElement>;
  offset?: number;
  deps?: unknown[];
  disabled?: boolean;
}
export const useHeightToBottom = ({
  ref,
  offset = 40,
  deps = [],
  disabled,
}: UseHeightToBottomProps) => {
  const [height, setHeight] = React.useState(0);

  React.useEffect(() => {
    if (!disabled) {
      const calculateHeight = () => {
        if (ref.current) {
          const rect = ref.current.getBoundingClientRect();
          const viewportHeight = window.innerHeight || document.documentElement.clientHeight;
          const heightToBottom = viewportHeight - rect.top;

          setHeight(heightToBottom);
        }
      };

      const throttledCalculateHeight = throttle(calculateHeight, 200);

      window.addEventListener('resize', throttledCalculateHeight);
      throttledCalculateHeight();

      return () => {
        window.removeEventListener('resize', throttledCalculateHeight);
      };
    }
  }, [ref, disabled, ...deps]);

  return disabled ? 0 : height - offset;
};
