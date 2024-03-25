// eslint-disable-next-line
export const throttle = <F extends (...args: any[]) => void>(func: F, ms: number) => {
  let timer: ReturnType<typeof setTimeout> | null;
  return (...args: Parameters<F>) => {
    if (timer) return;
    timer = setTimeout(() => {
      func(...args);
      timer = null;
    }, ms);
  };
};
