import * as React from 'react';

export const useSkipFirstEffect: typeof React.useEffect = (fn, dependencies) => {
  const isFirstMount = React.useRef(true);

  React.useEffect(() => {
    if (isFirstMount.current) {
      isFirstMount.current = false;
      return;
    }

    fn();
  }, dependencies);
};