// eslint-disable-next-line
export const throttle = <F extends (...args: any[]) => void>(func: F, ms: number) => {
  let timer: ReturnType<typeof setTimeout> | null = null;
  let lastArgs: Parameters<F>;
  return (...args: Parameters<F>) => {
    // 최신 인자를 보관해 트레일링 실행 시점에 반영한다
    lastArgs = args;
    if (timer) return;
    timer = setTimeout(() => {
      func(...lastArgs);
      timer = null;
    }, ms);
  };
};
