import React from 'react';
import { act, render, screen } from '@testing-library/react';
import { lazyWithPreload } from './lazyWithPreload';

const deferred = <T,>() => {
  let resolve!: (value: T) => void;
  const promise = new Promise<T>((res) => {
    resolve = res;
  });
  return { promise, resolve };
};

// 마운트될 때마다 세어, 컴포넌트가 다시 마운트되었는지(입력 중이던 값이 사라지는지) 확인한다.
let mountCount = 0;
const Popup = ({ label }: { label: string }) => {
  React.useEffect(() => {
    mountCount += 1;
  }, []);
  return <div>{label}</div>;
};

describe('lazyWithPreload', () => {
  beforeEach(() => {
    mountCount = 0;
  });

  test('should render without suspending when it has been preloaded', async () => {
    const { Component, preload } = lazyWithPreload(() => Promise.resolve(Popup));
    await preload();

    render(
      <React.Suspense fallback={<div>loading</div>}>
        <Component label="popup" />
      </React.Suspense>,
    );

    expect(screen.queryByText('loading')).toBeNull();
    expect(screen.getByText('popup')).toBeTruthy();
  });

  test('should suspend and then render when it has not been preloaded', async () => {
    const { promise, resolve } = deferred<typeof Popup>();
    const { Component } = lazyWithPreload(() => promise);

    render(
      <React.Suspense fallback={<div>loading</div>}>
        <Component label="popup" />
      </React.Suspense>,
    );
    expect(screen.getByText('loading')).toBeTruthy();

    await act(async () => {
      resolve(Popup);
    });
    expect(await screen.findByText('popup')).toBeTruthy();
  });

  // lazy 로 열린 뒤 모듈이 도착하고 부모가 다시 렌더되어도 같은 컴포넌트로 남아야 한다.
  // 다시 마운트되면 팝업에 입력 중이던 값이 사라진다(실시간 화면은 몇 초마다 다시 렌더된다).
  test('should not remount when the parent re-renders after the module has arrived', async () => {
    const { promise, resolve } = deferred<typeof Popup>();
    const { Component } = lazyWithPreload(() => promise);
    const Parent = ({ label }: { label: string }) => (
      <React.Suspense fallback={<div>loading</div>}>
        <Component label={label} />
      </React.Suspense>
    );

    const { rerender } = render(<Parent label="first" />);
    await act(async () => {
      resolve(Popup);
    });
    expect(await screen.findByText('first')).toBeTruthy();

    rerender(<Parent label="second" />);

    expect(screen.getByText('second')).toBeTruthy();
    expect(mountCount).toBe(1);
  });

  test('should reject preload when the module fails to load', async () => {
    const error = new Error('Failed to fetch dynamically imported module');
    const { preload } = lazyWithPreload(() => Promise.reject(error));

    await expect(preload()).rejects.toBe(error);
  });
});
