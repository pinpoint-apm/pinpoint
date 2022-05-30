import { RefObject, useEffect } from 'react';

export const useOutsideClick = <T extends HTMLElement = HTMLElement>(
  ref: RefObject<T>, 
  callback: (event: MouseEvent) => void,
) => {
  const handleClick = (e: MouseEvent) => {
    e.stopPropagation();
    if (ref.current && !ref.current.contains(e.target as Node)) {
      callback(e);
    }
  };

  useEffect(() => {
    document.addEventListener('click', handleClick);

    return () => {
      document.removeEventListener('click', handleClick);
    };
  });
};


export const useCaptureKeydown = (
  callback: (event: KeyboardEvent) => void,
) => {
  const handleKeydown = (e: KeyboardEvent) => {
    e.stopPropagation();
    callback?.(e);
  };

  useEffect(() => {
    document.addEventListener('keydown', handleKeydown);

    return () => {
      document.removeEventListener('keydown', handleKeydown);
    };
  });
};
