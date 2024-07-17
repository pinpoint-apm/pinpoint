import * as React from 'react';

export const useCaptureKeydown = (callback: (event: KeyboardEvent) => void) => {
  const handleKeydown = (e: KeyboardEvent) => {
    callback?.(e);
  };

  React.useEffect(() => {
    document.addEventListener('keydown', handleKeydown);

    return () => {
      document.removeEventListener('keydown', handleKeydown);
    };
  });
};
