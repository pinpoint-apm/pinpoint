import { renderHook, act } from '@testing-library/react';
import { useAtom } from 'jotai';
import {
  serverMapDataAtom,
  serverMapCurrentTargetAtom,
  serverMapCurrentTargetDataAtom,
} from './serverMap';
import { serverMapData, resultData } from './serverMapMock';

describe('Test serverMap atom', () => {
  describe('Test "serverMapDataAtom", "serverMapCurrentTargetAtom", "serverMapCurrentTargetDataAtom"', () => {
    test(`
      Test to Ensure serverMapCurrentTargetDataAtom data changed by serverMapCurrentTargetAtom changed
    `, () => {
      const { result: dataAtom } = renderHook(() => useAtom(serverMapDataAtom));
      const { result: currentTargetAtom } = renderHook(() => useAtom(serverMapCurrentTargetAtom));
      const { result: currentTargetDataAtom } = renderHook(() =>
        useAtom(serverMapCurrentTargetDataAtom),
      );

      act(() => {
        dataAtom.current[1](serverMapData);
        currentTargetAtom.current[1]({
          id: 'app1^SPRING_BOOT',
          applicationName: 'app1',
          serviceType: 'SPRING_BOOT',
          imgPath: '/img/servers/SPRING_BOOT.png',
          type: 'node',
        });
      });

      expect(currentTargetDataAtom.current[0]).toStrictEqual(resultData);
    });

    describe('USER 노드가 여럿일 때', () => {
      const firstUserNode = {
        key: 'app1_SPRING_BOOT^USER',
        nodeKey: 'app1_SPRING_BOOT^USER',
        applicationName: 'USER',
        serviceType: 'USER',
        totalCount: 3,
      };
      const secondUserNode = {
        key: 'app2_SPRING_BOOT^USER',
        nodeKey: 'app2_SPRING_BOOT^USER',
        applicationName: 'USER',
        serviceType: 'USER',
        totalCount: 121,
      };
      const subUserNode = {
        key: 'app3_SPRING_BOOT^USER',
        nodeKey: 'app3_SPRING_BOOT^USER',
        applicationName: 'USER',
        serviceType: 'USER',
        totalCount: 7,
      };
      const twoUserMapData = {
        applicationMapData: {
          ...serverMapData.applicationMapData,
          nodeDataArray: [
            firstUserNode,
            secondUserNode,
            { key: 'DEFAULT^group^SERVICE', serviceType: 'SERVICE', subNodes: [subUserNode] },
          ],
        },
      } as unknown as typeof serverMapData;

      const selectUser = (id?: string) => {
        const { result: dataAtom } = renderHook(() => useAtom(serverMapDataAtom));
        const { result: currentTargetAtom } = renderHook(() => useAtom(serverMapCurrentTargetAtom));
        const { result: currentTargetDataAtom } = renderHook(() =>
          useAtom(serverMapCurrentTargetDataAtom),
        );

        act(() => {
          dataAtom.current[1](twoUserMapData);
          currentTargetAtom.current[1]({
            id,
            applicationName: 'USER',
            serviceType: 'USER',
            type: 'node',
          });
        });

        return currentTargetDataAtom.current[0];
      };

      test('고른 USER 노드의 key로 찾는다 (첫 USER로 고정되지 않는다)', () => {
        expect(selectUser('app2_SPRING_BOOT^USER')).toBe(secondUserNode);
        expect(selectUser('app1_SPRING_BOOT^USER')).toBe(firstUserNode);
      });

      test('service group 안의 USER 노드도 찾는다', () => {
        expect(selectUser('app3_SPRING_BOOT^USER')).toBe(subUserNode);
      });

      test('id가 없으면 첫 USER로 폴백한다', () => {
        expect(selectUser(undefined)).toBe(firstUserNode);
      });
    });
  });
});
