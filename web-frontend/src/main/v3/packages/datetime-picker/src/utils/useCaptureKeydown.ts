import * as React from 'react';

/**
 * document keydown을 구독한다. 콜백은 최신 참조를 유지하므로 리스너는 마운트 시 1회만 등록된다.
 * 전역 stopPropagation은 하지 않는다 — 전파 제어는 콜백이 처리할 키에 한해 스스로 결정한다.
 */
export const useCaptureKeydown = (callback: (event: KeyboardEvent) => void) => {
  const callbackRef = React.useRef(callback);

  React.useEffect(() => {
    callbackRef.current = callback;
  });

  React.useEffect(() => {
    const handleKeydown = (event: KeyboardEvent) => callbackRef.current?.(event);
    document.addEventListener('keydown', handleKeydown);

    return () => {
      document.removeEventListener('keydown', handleKeydown);
    };
  }, []);
};
