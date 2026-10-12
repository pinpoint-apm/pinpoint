import React from 'react';

/**
 * 미리 받아 둘 수 있는 `React.lazy`.
 *
 * `React.lazy` 는 모듈을 이미 받아 두었어도 처음 렌더할 때 한 번은 suspend 한다. React 19 는 fallback 에서
 * 내용으로 바뀔 때 깜빡임을 줄이려고 약 300ms 를 기다리므로, 미리 받기만 해서는 처음 열 때 비는 순간이
 * 없어지지 않는다. 그래서 `preload` 가 끝난 뒤에 마운트되면 lazy 를 거치지 않고 받아 둔 컴포넌트를 바로
 * 렌더한다. 아직 받지 못했으면 평소처럼 lazy 로 suspend 하므로 바깥에 `Suspense` 가 있어야 한다.
 *
 * lazy 로 suspend 했다가 모듈이 도착하면 React 는 이 컴포넌트부터 다시 렌더하는데, 그때는 `loaded` 가
 * 이미 채워져 있어 처음부터 받아 둔 컴포넌트로 마운트된다. lazy 가 commit 되었다가 받아 둔 컴포넌트로
 * 종류가 바뀌는(다시 마운트되어 입력 중이던 값이 사라지는) 일은 생기지 않는다.
 *
 * @example
 * const { Component: Setting, preload: preloadSetting } = lazyWithPreload(() =>
 *   import('./Setting').then((module) => module.Setting),
 * );
 * <button onMouseEnter={() => preloadSetting().catch(() => {})} />
 */
export const lazyWithPreload = <P extends object>(load: () => Promise<React.ComponentType<P>>) => {
  let loaded: React.ComponentType<P> | undefined;

  const preload = () =>
    load().then((component) => {
      loaded = component;
      return component;
    });

  const LazyComponent = React.lazy(() => preload().then((component) => ({ default: component })));

  const Component = (props: P) => {
    const Resolved = loaded ?? (LazyComponent as React.ComponentType<P>);
    return React.createElement(Resolved, props);
  };

  return { Component, preload };
};
