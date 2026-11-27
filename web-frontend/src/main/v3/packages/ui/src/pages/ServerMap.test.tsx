import React from 'react';
import { act, render, screen } from '@testing-library/react';
import { createStore, Provider, useAtomValue, useSetAtom } from 'jotai';
import { MemoryRouter, useLocation } from 'react-router';
import {
  serverMapCurrentTargetAtom,
  serverMapDataAtom,
  serverMapFocusNodeIdAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { GetServerMap } from '@pinpoint-fe/ui/src/constants';
import { getServiceMapPath } from '@pinpoint-fe/ui/src/utils';
import { ServerMapPage } from './ServerMap';

/**
 * 기준 application이 필요 없는 map(비DEFAULT servicemap)에 application이 실린 링크로 들어왔을 때,
 * 화면이 그 application을 경로에서 지우면서도 그 노드를 선택해 두는지 본다. (이슈 #11280)
 *
 * map과 우측 패널은 각자 테스트가 있으므로 표지로 바꾸고, 페이지가 세운 선택만 본다.
 */

jest.mock('react-i18next', () => ({
  useTranslation: () => ({ t: (key: string) => key }),
}));

// 루트 barrel은 패키지 전체(cytoscape, echarts …)를 끌어온다. 이 테스트에는 필요 없다.
jest.mock('@pinpoint-fe/ui', () => ({
  MainHeader: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  DatetimePicker: () => null,
  FilterWizard: () => null,
  HelpPopover: () => null,
  ApplicationCombinedList: () => null,
  ServerMap: () => null,
  LayoutWithHorizontalResizable: ({ children }: { children: React.ReactNode[] }) => (
    <div>{children[0]}</div>
  ),
}));

jest.mock('@pinpoint-fe/ui/src/components/ServerMap/ServerMapChartBoard', () => ({
  ServerMapChartsBoard: () => null,
}));

// hooks barrel도 차트 라이브러리까지 끌어온다. 페이지가 쓰는 훅만 그 파일에서 직접 읽는다.
jest.mock('@pinpoint-fe/ui/src/hooks', () => ({
  useConfiguration: () => ({}),
  useServerMapSearchParameters: jest.requireActual(
    '@pinpoint-fe/ui/src/hooks/searchParameters/useServerMapSearchParameters',
  ).useServerMapSearchParameters,
}));

jest.mock('@pinpoint-fe/ui/src/hooks/serverMap', () => ({
  useServerMapCurrentTarget: jest.requireActual(
    '@pinpoint-fe/ui/src/hooks/serverMap/useServerMapCurrentTarget',
  ).useServerMapCurrentTarget,
  useFilterWizardOnClickApply: () => jest.fn(),
  useServerMapOnClickMenuItem: () => jest.fn(),
}));

const SERVICE = 'blogService';
const PERIOD = 'from=2026-11-26-10-00-00&to=2026-11-26-10-05-00';
const TARGET = { applicationName: 'b-1', serviceType: 'TOMCAT' };

const appNode = (key: string, applicationName: string, serviceType = 'TOMCAT') =>
  ({ key, applicationName, serviceType }) as GetServerMap.NodeData;

const mapOf = (nodeDataArray: GetServerMap.NodeData[]) =>
  ({
    applicationMapData: { nodeDataArray, linkDataArray: [] },
  }) as unknown as GetServerMap.Response;

/** map이 도착하는 것을 흉내 낸다. 테스트가 `deliver`로 원하는 때에 응답을 넣는다. */
let deliver: (data: GetServerMap.Response | undefined) => void = () => {};
const FakeMapView = () => {
  const setData = useSetAtom(serverMapDataAtom);
  deliver = (data) => setData(data);
  return null;
};

const Probe = () => {
  const target = useAtomValue(serverMapCurrentTargetAtom);
  const focusNodeId = useAtomValue(serverMapFocusNodeIdAtom);
  const { pathname } = useLocation();
  return (
    <>
      <div data-testid="path">{pathname}</div>
      <div data-testid="focus">{focusNodeId ?? ''}</div>
      <div data-testid="target">{target ? JSON.stringify(target) : ''}</div>
    </>
  );
};

const renderPage = (path: string, initialData?: GetServerMap.Response) => {
  const store = createStore();
  store.set(serverMapDataAtom, initialData);

  render(
    <Provider store={store}>
      <MemoryRouter initialEntries={[path]}>
        <ServerMapPage
          MapView={FakeMapView as never}
          requiresApplication={false}
          serviceName={SERVICE}
          getPagePath={(application, queryParams) =>
            getServiceMapPath(SERVICE, application, queryParams)
          }
        />
        <Probe />
      </MemoryRouter>
    </Provider>,
  );
};

const currentTarget = () => {
  const text = screen.getByTestId('target').textContent;
  return text ? JSON.parse(text) : undefined;
};

describe('ServerMapPage without a base application', () => {
  test('selects the application the link carried, after removing it from the path', () => {
    renderPage(`/serviceMap/${SERVICE}/b-1@TOMCAT?${PERIOD}`);

    // 통계 조회의 기준이 되지 않도록 경로에서는 지운다.
    expect(screen.getByTestId('path').textContent).toBe(`/serviceMap/${SERVICE}`);
    expect(currentTarget()).toBeUndefined();

    act(() =>
      deliver(
        mapOf([appNode(`${SERVICE}^a-1^TOMCAT`, 'a-1'), appNode(`${SERVICE}^b-1^TOMCAT`, 'b-1')]),
      ),
    );

    expect(currentTarget()).toEqual({
      id: `${SERVICE}^b-1^TOMCAT`,
      ...TARGET,
      imgPath: expect.stringContaining('TOMCAT'),
      type: 'node',
    });
    // 기준 노드가 없는 map은 전체를 기준으로 센터링하므로 그 노드로 옮겨 달라고 요청한다.
    expect(screen.getByTestId('focus').textContent).toBe(`${SERVICE}^b-1^TOMCAT`);
  });

  // map이 이미 캐시에 있어 경로를 지우기 전에 도착한 경우. 그때 세우면 지우기 전 경로로 도장이
  // 찍혀 곧 남의 선택이 된다.
  test('selects the application when the map was there before the path was cleaned', () => {
    renderPage(
      `/serviceMap/${SERVICE}/b-1@TOMCAT?${PERIOD}`,
      mapOf([appNode(`${SERVICE}^b-1^TOMCAT`, 'b-1')]),
    );

    expect(screen.getByTestId('path').textContent).toBe(`/serviceMap/${SERVICE}`);
    expect(currentTarget()).toMatchObject({ id: `${SERVICE}^b-1^TOMCAT`, ...TARGET });
  });

  // 다른 service의 application은 접힌 service group의 자식으로 온다. group은 선택 대상이 아니므로
  // 팝업에서 그 자식을 고른 것과 같은 선택이어야 한다.
  test('selects the child, not the collapsed service group that holds it', () => {
    renderPage(`/serviceMap/${SERVICE}/b-1@TOMCAT?${PERIOD}`);

    const child = appNode('B^b-1^TOMCAT', 'b-1');
    act(() =>
      deliver(
        mapOf([
          appNode(`${SERVICE}^a-1^TOMCAT`, 'a-1'),
          { key: 'B', applicationName: 'B', serviceType: 'SERVICE', subNodes: [child] } as never,
        ]),
      ),
    );

    expect(currentTarget()).toMatchObject({ id: 'B^b-1^TOMCAT', ...TARGET, type: 'node' });
    expect(screen.getByTestId('focus').textContent).toBe('B^b-1^TOMCAT');
  });

  test('selects it only once', () => {
    renderPage(`/serviceMap/${SERVICE}/b-1@TOMCAT?${PERIOD}`);
    const map = mapOf([appNode(`${SERVICE}^b-1^TOMCAT`, 'b-1')]);

    act(() => deliver(map));
    expect(currentTarget()).toMatchObject(TARGET);

    // 다른 노드로 옮겨 고른 뒤 map이 다시 와도(새 객체) 링크의 application으로 되돌리지 않는다.
    act(() => deliver(mapOf([appNode(`${SERVICE}^b-1^TOMCAT`, 'b-1')])));
    expect(currentTarget()).toMatchObject(TARGET);
  });

  test('leaves nothing selected when the map does not have the application', () => {
    renderPage(`/serviceMap/${SERVICE}/b-1@TOMCAT?${PERIOD}`);

    act(() => deliver(mapOf([appNode(`${SERVICE}^a-1^TOMCAT`, 'a-1')])));

    expect(currentTarget()).toBeUndefined();
    expect(screen.getByTestId('focus').textContent).toBe('');
  });

  test('selects nothing when the link carried no application', () => {
    renderPage(`/serviceMap/${SERVICE}?${PERIOD}`);

    act(() => deliver(mapOf([appNode(`${SERVICE}^b-1^TOMCAT`, 'b-1')])));

    expect(currentTarget()).toBeUndefined();
  });
});
