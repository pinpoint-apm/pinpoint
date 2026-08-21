import { renderHook, act } from '@testing-library/react';
import { getDefaultStore } from 'jotai';
import {
  configurationAtom,
  CurrentTarget,
  serverMapCurrentTargetAtom,
  serverMapDataAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { Configuration, GetServerMap } from '@pinpoint-fe/ui/src/constants';
import { useServerMapTargetServiceName } from './useServerMapTargetServiceName';

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
    setEnableServiceMap(true);
  });

  test('is undefined while nothing is picked, so the screen service keeps being used', () => {
    setMap([node('aService', 'a-1')]);
    setTarget(undefined);

    expect(render()).toBeUndefined();
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
