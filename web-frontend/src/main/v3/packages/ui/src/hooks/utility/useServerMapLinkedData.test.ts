import { useServerMapLinkedData } from './useServerMapLinkedData';
import { GetServerMap } from '@pinpoint-fe/ui/src/constants';

const makeLinkData = (
  from: string,
  to: string,
  sourceName: string,
  targetName: string,
): GetServerMap.LinkData =>
  ({
    from,
    to,
    sourceInfo: { applicationName: sourceName, serviceTypeCode: 1000 },
    targetInfo: { applicationName: targetName, serviceTypeCode: 1010 },
  }) as unknown as GetServerMap.LinkData;

const mockServerMapData = {
  linkDataArray: [
    makeLinkData('AppA^SPRING_BOOT', 'AppB^SPRING_BOOT', 'AppA', 'AppB'),
    makeLinkData('AppC^NODE_JS', 'AppA^SPRING_BOOT', 'AppC', 'AppA'),
    makeLinkData('AppD^SPRING_BOOT', 'AppE^SPRING_BOOT', 'AppD', 'AppE'),
  ],
} as unknown as GetServerMap.ApplicationMapData;

const mockTargetNode = { key: 'AppA^SPRING_BOOT' } as unknown as GetServerMap.NodeData;

describe('useServerMapLinkedData', () => {
  test('returns undefined when serverMapData is not provided', () => {
    const result = useServerMapLinkedData({
      serverMapData: undefined,
      currentTargetData: mockTargetNode,
    });
    expect(result).toBeUndefined();
  });

  test('returns empty from/to arrays when currentTargetData is not provided', () => {
    const result = useServerMapLinkedData({
      serverMapData: mockServerMapData,
      currentTargetData: undefined,
    });
    expect(result).toEqual({ from: [], to: [] });
  });

  test('populates "to" array with nodes that the target calls out to', () => {
    const result = useServerMapLinkedData({
      serverMapData: mockServerMapData,
      currentTargetData: mockTargetNode,
    });
    expect(result?.to).toHaveLength(1);
    expect(result?.to[0].applicationName).toBe('AppB');
  });

  test('populates "from" array with nodes that call into the target', () => {
    const result = useServerMapLinkedData({
      serverMapData: mockServerMapData,
      currentTargetData: mockTargetNode,
    });
    expect(result?.from).toHaveLength(1);
    expect(result?.from[0].applicationName).toBe('AppC');
  });

  test('returns empty from/to arrays when target node has no links', () => {
    const isolatedTarget = { key: 'Isolated^NODE_JS' } as unknown as GetServerMap.NodeData;
    const result = useServerMapLinkedData({
      serverMapData: mockServerMapData,
      currentTargetData: isolatedTarget,
    });
    expect(result).toEqual({ from: [], to: [] });
  });

  test('handles multiple outbound links from the same node', () => {
    const dataWithMultipleOutbound = {
      linkDataArray: [
        makeLinkData('AppA^SPRING_BOOT', 'AppB^SPRING_BOOT', 'AppA', 'AppB'),
        makeLinkData('AppA^SPRING_BOOT', 'AppC^NODE_JS', 'AppA', 'AppC'),
      ],
    } as unknown as GetServerMap.ApplicationMapData;

    const result = useServerMapLinkedData({
      serverMapData: dataWithMultipleOutbound,
      currentTargetData: mockTargetNode,
    });
    expect(result?.to).toHaveLength(2);
    expect(result?.from).toHaveLength(0);
  });
});
