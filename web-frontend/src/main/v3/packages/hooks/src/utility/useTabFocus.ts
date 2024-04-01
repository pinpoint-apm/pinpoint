import React from 'react';

export const useTabFocus = (delay = 3000) => {
  const [isTabFocused, setIsTabFocused] = React.useState(true);

  React.useEffect(() => {
    let timer: NodeJS.Timeout;

    const handleFocusChange = () => {
      if (!document.hidden) {
        setIsTabFocused(!document.hidden);
        clearTimeout(timer);
      } else {
        timer = setTimeout(() => setIsTabFocused(!document.hidden), delay);
      }
    };

    handleFocusChange();

    document.addEventListener('visibilitychange', handleFocusChange);

    return () => {
      document.removeEventListener('visibilitychange', handleFocusChange);
      clearTimeout(timer);
    };
  }, [delay]);
  return isTabFocused;
};
