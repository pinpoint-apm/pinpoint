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
  });
});
