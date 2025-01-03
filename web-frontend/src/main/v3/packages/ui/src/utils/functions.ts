export const debounce = (func: () => void, delay?: number) => {
  let timeoutId: NodeJS.Timeout;

  return () => {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => {
      func();
    }, delay || 250);
  };
};
