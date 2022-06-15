import { useRef, useEffect } from 'react';

export const useSkipFirstEffect: typeof useEffect = (fn, dependencies) => {
  const isFirstMount = useRef(true);

  useEffect(() => {
    if (isFirstMount.current) {
      isFirstMount.current = false;
      return;
    }

    fn();
  }, dependencies);
};