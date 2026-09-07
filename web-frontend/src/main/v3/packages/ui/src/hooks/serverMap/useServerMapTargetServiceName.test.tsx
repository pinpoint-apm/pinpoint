import { renderHook, act } from '@testing-library/react';
import { getDefaultStore } from 'jotai';
import {
  configurationAtom,
  CurrentTarget,
  selectedServiceAtom,
  serverMapCurrentTargetAtom,
  serverMapDataAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { Configuration, GetServerMap } from '@pinpoint-fe/ui/src/constants';
import { useServerMapTargetServiceName } from './useServerMapTargetServiceName';

// 화면의 service는 경로에서 읽으므로(`useServiceNameForLink`) 위치를 갈아 끼울 수 있게 한다.
const mockLocation = { pathname: '/serviceMap/aService' };
jest.mock('react-router', () => ({
  ...jest.requireActual('react-router'),
  useLocation: () => mockLocation,
}));

const store = getDefaultStore();

const configWithServiceMap = (enable: boolean) =>
  ({ 'experimental.enableServiceMap.value': enable }) as unknown as Configuration;

const setEnableServiceMap = (enable: boolean) => {
  act(() => {
    store.set(configurationAtom, configWithServiceMap(enable));
  });
};

const node = (serviceName: string, applicationName: string) =>
  ({
    key: `${serviceName}^${applicationName}^SPRING_BOOT`,
    serviceName,
    applicationName,
    serviceType: 'SPRING_BOOT',
  }) as GetServerMap.NodeData;

const setMap = (
  nodeDataArray: GetServerMap.NodeData[],
  linkDataArray: GetServerMap.LinkData[] = [],
) => {
  act(() => {
    store.set(serverMapDataAtom, {
      applicationMapData: { nodeDataArray, linkDataArray },
    } as GetServerMap.Response);
  });
};

const setTarget = (target?: CurrentTarget) => {
  act(() => {
    store.set(serverMapCurrentTargetAtom, target);
  });
};

const render = () => renderHook(() => useServerMapTargetServiceName()).result.current;

describe('useServerMapTargetServiceName', () => {
  beforeEach(() => {
    mockLocation.pathname = '/serviceMap/aService';
    act(() => {
      store.set(selectedServiceAtom, 'aService');
    });
    setEnableServiceMap(true);
  });

  // 값이 도중에 undefined에서 화면의 service로 바뀌면 같은 요청이 서로 다른 캐시 키로 두 번
  // 쌓여 조회가 한 번 더 나간다(이슈 #10587). 처음부터 화면의 service를 준다.
  test('is the screen service while nothing is picked yet', () => {
    setMap([node('aService', 'a-1')]);
    setTarget(undefined);

    expect(render()).toBe('aService');
  });

  // 경로에 serviceName이 없는 화면(servermap)에서는 전역 선택값이 화면의 service다.
  test('falls back to the globally selected service when the path carries none', () => {
    mockLocation.pathname = '/serverMap/a-1@SPRING_BOOT';
    setMap([node('aService', 'a-1')]);
    setTarget(undefined);

    expect(render()).toBe('aService');
  });

  // 설정이 곧 "어느 map이 보이는가"다 — 켜져 있으면 servicemap만 보이고 servermap URL은 렌더
  // 전에 옮겨지므로(`getHiddenMapPageRedirect`), 여기까지 온 노드는 servicemap이 그린 것이다.
  // 그래서 경로를 따로 보지 않고 설정 하나로 갈린다. (이슈 #10587)
  test('takes the picked node service without looking at the path', () => {
    mockLocation.pathname = '/serviceMap';
    setMap([node('bService', 'b-1')]);
    setTarget({ id: 'bService^b-1^SPRING_BOOT', type: 'node' });

    expect(render()).toBe('bService');
  });

  // servicemap 실시간도 group을 펼쳐 다른 service의 노드를 고를 수 있다.
  test('takes the picked node service on servicemap realtime', () => {
    mockLocation.pathname = '/serviceMap/realtime/aService';
    setMap([node('aService', 'a-1'), node('bService', 'b-1')]);
    setTarget({ id: 'bService^b-1^SPRING_BOOT', type: 'node' });

    expect(render()).toBe('bService');
  });

  test('is the service of the picked node', () => {
    setMap([node('aService', 'a-1')]);
    setTarget({ id: 'aService^a-1^SPRING_BOOT', type: 'node' });

    expect(render()).toBe('aService');
  });

  // service group을 펼쳐 고른 자식 노드. 이 화면(aService)과 다른 service에 속한다.
  test('is the service of a child node picked inside a collapsed service group', () => {
    setMap([
      node('aService', 'a-1'),
      {
        ...node('bService', 'bService'),
        key: 'bService',
        subNodes: [node('bService', 'b-1')],
      } as GetServerMap.NodeData,
    ]);
    setTarget({ id: 'bService^b-1^SPRING_BOOT', type: 'node' });

    expect(render()).toBe('bService');
  });

  // 설정이 꺼져 있으면 백엔드가 모든 요청을 기본 service로 해석하므로 실을 값이 없다.
  test('is undefined when enableServiceMap is off', () => {
    setEnableServiceMap(false);
    setMap([node('aService', 'a-1')]);
    setTarget({ id: 'aService^a-1^SPRING_BOOT', type: 'node' });

    expect(render()).toBeUndefined();
  });

  test('is the service of the source node for a link', () => {
    setMap(
      [node('aService', 'a-1')],
      [
        {
          key: 'aService^a-1^SPRING_BOOT~bService^b-1^SPRING_BOOT',
          sourceInfo: { serviceName: 'aService', applicationName: 'a-1' },
          targetInfo: { serviceName: 'bService', applicationName: 'b-1' },
        } as GetServerMap.LinkData,
      ],
    );
    setTarget({ id: 'aService^a-1^SPRING_BOOT~bService^b-1^SPRING_BOOT', type: 'edge' });

    expect(render()).toBe('aService');
  });
});
