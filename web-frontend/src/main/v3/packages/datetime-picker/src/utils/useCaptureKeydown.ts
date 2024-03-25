import * as React from 'react';

export const useCaptureKeydown = (callback: (event: KeyboardEvent) => void) => {
  const handleKeydown = (e: KeyboardEvent) => {
    e.stopPropagation();
    callback?.(e);
  };

  React.useEffect(() => {
    document.addEventListener('keydown', handleKeydown);

    return () => {
      document.removeEventListener('keydown', handleKeydown);
    };
  });
};
