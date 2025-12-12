import { getScatterData } from './scatter';
import {
  GetScatter,
  FilteredMapType as FilteredMap,
  SCATTER_DATA_TOTAL_KEY,
  ScatterDataByAgent,
} from '@pinpoint-fe/ui/src/constants';

describe('Test scatter helper utils', () => {
  describe('Test "getScatterData"', () => {
    test('Process scatter data without prevData', () => {
      const newData: GetScatter.Response = {
        scatter: {
          dotList: [
            [100, 200, 0, 0, 1, 1], // [x, y, metadataIndex, transactionId, success(1), hidden(1)]
          ],
          metadata: {
            0: ['agent-1', 'agent-id-1', 1234567890],
          },
        },
        from: 1000,
        to: 2000,
        complete: true,
        currentServerTime: 1234567890,
        resultFrom: 1000,
        resultTo: 2000,
      };

      const result = getScatterData(newData);

      expect(result.curr[SCATTER_DATA_TOTAL_KEY]).toHaveLength(1);
      expect(result.curr['agent-1']).toHaveLength(1);
      expect(result.acc[SCATTER_DATA_TOTAL_KEY]).toHaveLength(1);
      expect(result.acc['agent-1']).toHaveLength(1);
      expect(result.dateRange).toEqual([1000, 2000]);

      const scatterData = result.curr[SCATTER_DATA_TOTAL_KEY]?.[0];
      expect(scatterData?.x).toBe(1100); // from + dotList[0]
      expect(scatterData?.y).toBe(200);
      expect(scatterData?.type).toBe('success');
      expect(scatterData?.hidden).toBe(false);
    });

    test('Process scatter data with prevData', () => {
      const prevData: ScatterDataByAgent = {
        curr: {
          [SCATTER_DATA_TOTAL_KEY]: [{ x: 50, y: 100, type: 'success', hidden: false }],
          'agent-0': [{ x: 50, y: 100, type: 'success', hidden: false }],
        },
        acc: {
          [SCATTER_DATA_TOTAL_KEY]: [{ x: 50, y: 100, type: 'success', hidden: false }],
          'agent-0': [{ x: 50, y: 100, type: 'success', hidden: false }],
        },
      };

      const newData: GetScatter.Response = {
        scatter: {
          dotList: [[100, 200, 0, 0, 1, 1]],
          metadata: {
            0: ['agent-1', 'agent-id-1', 1234567890],
          },
        },
        from: 1000,
        to: 2000,
        complete: true,
        currentServerTime: 1234567890,
        resultFrom: 1000,
        resultTo: 2000,
      };

      const result = getScatterData(newData, prevData);

      expect(result.curr[SCATTER_DATA_TOTAL_KEY]).toHaveLength(1);
      expect(result.acc[SCATTER_DATA_TOTAL_KEY]).toHaveLength(2); // prev + new
    });

    test('Process failed transaction data', () => {
      const newData: GetScatter.Response = {
        scatter: {
          dotList: [
            [100, 200, 0, 0, 0, 1], // success = 0 (failed)
          ],
          metadata: {
            0: ['agent-1', 'agent-id-1', 1234567890],
          },
        },
        from: 1000,
        to: 2000,
        complete: true,
        currentServerTime: 1234567890,
        resultFrom: 1000,
        resultTo: 2000,
      };

      const result = getScatterData(newData);
      const scatterData = result.curr[SCATTER_DATA_TOTAL_KEY]?.[0];

      expect(scatterData?.type).toBe('failed');
    });

    test('Process hidden transaction data', () => {
      const newData: GetScatter.Response = {
        scatter: {
          dotList: [
            [100, 200, 0, 0, 1, 0], // hidden = 0 (hidden)
          ],
          metadata: {
            0: ['agent-1', 'agent-id-1', 1234567890],
          },
        },
        from: 1000,
        to: 2000,
        complete: true,
        currentServerTime: 1234567890,
        resultFrom: 1000,
        resultTo: 2000,
      };

      const result = getScatterData(newData);
      const scatterData = result.curr[SCATTER_DATA_TOTAL_KEY]?.[0];

      expect(scatterData?.hidden).toBe(true);
    });

    test('Process multiple agents', () => {
      const newData: GetScatter.Response = {
        scatter: {
          dotList: [
            [100, 200, 0, 0, 1, 1],
            [150, 250, 1, 0, 1, 1],
          ],
          metadata: {
            0: ['agent-1', 'agent-id-1', 1234567890],
            1: ['agent-2', 'agent-id-2', 1234567891],
          },
        },
        from: 1000,
        to: 2000,
        complete: true,
        currentServerTime: 1234567890,
        resultFrom: 1000,
        resultTo: 2000,
      };

      const result = getScatterData(newData);

      expect(result.curr[SCATTER_DATA_TOTAL_KEY]).toHaveLength(2);
      expect(result.curr['agent-1']).toHaveLength(1);
      expect(result.curr['agent-2']).toHaveLength(1);
      expect(result.acc[SCATTER_DATA_TOTAL_KEY]).toHaveLength(2);
    });

    test('Process FilteredMap scatter data with isFilterMap option', () => {
      const newData: FilteredMap.ScatterData = {
        scatter: {
          dotList: [
            [100, 200, 0, 12345, 1, 1], // [x, y, metadataIndex, transactionId, success, hidden]
          ],
          metadata: {
            0: ['agent-1', 'agent-id-1', 1234567890],
          },
        },
        from: 1000,
        to: 2000,
        resultFrom: 1000,
        resultTo: 2000,
      };

      const result = getScatterData(newData, undefined, { isFilterMap: true });

      const scatterData = result.curr[SCATTER_DATA_TOTAL_KEY]?.[0];
      expect(scatterData?.transactionId).toBe(12345);
      expect(scatterData?.collectorAcceptTime).toBe(1234567890);
      expect(scatterData?.agentId).toBe('agent-id-1');
    });

    test('Handle incomplete data (no dateRange)', () => {
      const newData: GetScatter.Response = {
        scatter: {
          dotList: [[100, 200, 0, 0, 1, 1]],
          metadata: {
            0: ['agent-1', 'agent-id-1', 1234567890],
          },
        },
        from: 1000,
        to: 2000,
        complete: false,
        currentServerTime: 1234567890,
        resultFrom: 1000,
        resultTo: 2000,
      };

      const result = getScatterData(newData);

      expect(result.dateRange).toBeUndefined();
    });

    test('Handle empty dotList', () => {
      const newData: GetScatter.Response = {
        scatter: {
          dotList: [],
          metadata: {},
        },
        from: 1000,
        to: 2000,
        complete: true,
        currentServerTime: 1234567890,
        resultFrom: 1000,
        resultTo: 2000,
      };

      const result = getScatterData(newData);

      expect(result.curr[SCATTER_DATA_TOTAL_KEY]).toHaveLength(0);
      expect(result.acc[SCATTER_DATA_TOTAL_KEY]).toHaveLength(0);
    });

    test('Return new object references (shallow copy)', () => {
      const newData: GetScatter.Response = {
        scatter: {
          dotList: [[100, 200, 0, 0, 1, 1]],
          metadata: {
            0: ['agent-1', 'agent-id-1', 1234567890],
          },
        },
        from: 1000,
        to: 2000,
        complete: true,
        currentServerTime: 1234567890,
        resultFrom: 1000,
        resultTo: 2000,
      };

      const result1 = getScatterData(newData);
      const result2 = getScatterData(newData);

      expect(result1.curr).not.toBe(result2.curr);
      expect(result1.acc).not.toBe(result2.acc);
    });
  });
});
