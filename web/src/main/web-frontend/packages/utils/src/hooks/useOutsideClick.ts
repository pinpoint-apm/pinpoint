import * as React from 'react';

export const useOutsideClick = <T extends HTMLElement = HTMLElement>(
  ref: React.RefObject<T>, 
  callback: (event: MouseEvent) => void,
) => {
  const handleClick = (e: MouseEvent) => {
    e.stopPropagation();
    if (ref.current && !ref.current.contains(e.target as Node)) {
      callback(e);
    }
  };

  React.useEffect(() => {
    document.addEventListener('click', handleClick);

    return () => {
      document.removeEventListener('click', handleClick);
    };
  });
};