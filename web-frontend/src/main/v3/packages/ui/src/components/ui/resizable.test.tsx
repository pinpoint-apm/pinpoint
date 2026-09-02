/**
 * ResizablePanelGroup / ResizablePanel / ResizableHandle 의 동작 테스트.
 *
 * 라이브러리를 올릴 때 방향별 핸들 모양, id 조회, 저장된 레이아웃 복원이 그대로인지 확인할 수
 * 있어야 해서 렌더 결과만 본다. `defaultSize` 가 실제 몇 퍼센트로 풀리는지는 jsdom 에서 확인할
 * 수 없다(라이브러리가 실제 픽셀을 재야 하고 jsdom 의 요소 크기는 0 이다) — 그 부분은
 * `toPanelPercent` 를 직접 검증한다.
 */
import { render } from '@testing-library/react';
import { ResizableHandle, ResizablePanel, ResizablePanelGroup } from './resizable';
import {
  getResizableHandleElement,
  getResizablePanelElement,
  getResizablePanelGroupElement,
  toPanelPercent,
} from './resizableElements';

jest.mock('../../lib', () => ({
  cn: (...args: unknown[]) => args.filter((a) => typeof a === 'string').join(' '),
}));

class ResizeObserverStub {
  observe() {}
  unobserve() {}
  disconnect() {}
}

beforeAll(() => {
  // 라이브러리가 패널 크기를 재는 데 쓰지만 jsdom 에는 없다.
  (globalThis as unknown as { ResizeObserver: unknown }).ResizeObserver = ResizeObserverStub;
  // 포인터 정밀도(pointer:coarse) 판단에 쓴다.
  window.matchMedia = ((query: string) =>
    ({
      matches: false,
      media: query,
      onchange: null,
      addEventListener: () => {},
      removeEventListener: () => {},
      addListener: () => {},
      removeListener: () => {},
      dispatchEvent: () => false,
    }) as unknown as MediaQueryList) as typeof window.matchMedia;
});

const renderGroup = (props: { direction?: 'horizontal' | 'vertical'; autoSaveId?: string } = {}) =>
  render(
    <ResizablePanelGroup id="group-id" {...props}>
      <ResizablePanel id="left" defaultSize={25} minSize={10} maxSize={40} />
      <ResizableHandle id="handle-id" withHandle />
      <ResizablePanel id="right" defaultSize={75} />
    </ResizablePanelGroup>,
  );

describe('toPanelPercent', () => {
  it('숫자에 퍼센트를 붙인다', () => {
    expect(toPanelPercent(25)).toBe('25%');
    expect(toPanelPercent(0)).toBe('0%');
  });

  it('단위를 직접 준 문자열은 그대로 둔다', () => {
    expect(toPanelPercent('500px')).toBe('500px');
    expect(toPanelPercent('40%')).toBe('40%');
  });

  it('값이 없으면 그대로 없다', () => {
    expect(toPanelPercent(undefined)).toBeUndefined();
  });
});

describe('ResizablePanelGroup', () => {
  it('그룹과 패널에 조회용 data 속성과 id 를 남긴다', () => {
    renderGroup();
    expect(getResizablePanelGroupElement('group-id')).not.toBeNull();
    expect(getResizablePanelElement('left')).not.toBeNull();
    expect(getResizablePanelElement('right')).not.toBeNull();
    expect(getResizableHandleElement('handle-id')).not.toBeNull();
  });

  it('id 로 찾을 때 종류를 구분한다', () => {
    renderGroup();
    // 그룹 id 로 패널을 찾으면 안 된다 — 서버맵은 그룹·핸들·패널에 같은 id 를 쓴다.
    expect(getResizablePanelElement('group-id')).toBeNull();
    expect(getResizablePanelGroupElement('left')).toBeNull();
  });

  it('기본 방향은 가로다', () => {
    renderGroup();
    expect(getResizablePanelGroupElement('group-id')?.style.flexDirection).toBe('row');
  });

  it('세로 방향이면 세로로 쌓는다', () => {
    renderGroup({ direction: 'vertical' });
    expect(getResizablePanelGroupElement('group-id')?.style.flexDirection).toBe('column');
  });
});

describe('ResizableHandle', () => {
  // v4 의 Separator 에는 방향을 알려주는 data 속성이 없다. 핸들 모양을 방향에 따라 바꾸는
  // Tailwind 클래스가 이 속성에 걸려 있으므로 그룹이 내려준 방향이 실제로 붙어야 한다.
  it('그룹의 방향을 data 속성으로 노출한다', () => {
    renderGroup();
    expect(getResizableHandleElement('handle-id')?.dataset.panelGroupDirection).toBe('horizontal');
  });

  it('세로 그룹의 핸들은 vertical 로 표시된다', () => {
    renderGroup({ direction: 'vertical' });
    expect(getResizableHandleElement('handle-id')?.dataset.panelGroupDirection).toBe('vertical');
  });

  it('role=separator 로 렌더된다', () => {
    renderGroup();
    expect(getResizableHandleElement('handle-id')?.getAttribute('role')).toBe('separator');
  });
});

describe('레이아웃 저장', () => {
  beforeEach(() => localStorage.clear());

  it('autoSaveId 가 없으면 localStorage 에 쓰지 않는다', () => {
    renderGroup();
    expect(Object.keys(localStorage)).toEqual([]);
  });

  // 3 까지 저장된 형식(패널 id 목록 → layout 배열)을 4 가 읽어 주므로, 기존 사용자의 패널
  // 크기가 업그레이드 후에도 살아난다.
  it('예전 저장 형식도 복원한다', () => {
    localStorage.setItem(
      'react-resizable-panels:pp.test',
      JSON.stringify({ 'left,right': { layout: [40, 60] } }),
    );
    renderGroup({ autoSaveId: 'pp.test' });
    expect(getResizablePanelElement('left')?.style.flexGrow).toBe('40');
    expect(getResizablePanelElement('right')?.style.flexGrow).toBe('60');
  });
});
