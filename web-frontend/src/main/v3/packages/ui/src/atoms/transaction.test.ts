import { renderHook, act } from '@testing-library/react';
import { useAtom } from 'jotai';
import {
  transactionListDatasAtom,
  transactionInfoDatasAtom,
  transactionInfoCurrentTabId,
  transactionInfoCallTreeFocusId,
} from './transaction';
import { Transaction, TransactionInfoType as TransactionInfo } from '@pinpoint-fe/ui/src/constants';

describe('Test transaction atoms', () => {
  describe('Test "transactionListDatasAtom"', () => {
    test('should initialize with undefined', () => {
      const { result } = renderHook(() => useAtom(transactionListDatasAtom));
      expect(result.current[0]).toBeUndefined();
    });

    test('should update with transaction list data', () => {
      const { result } = renderHook(() => useAtom(transactionListDatasAtom));
      const mockData: Transaction[] = [
        {
          agentId: 'agent1',
          applicationId: 'app1',
          traceId: 'trace1',
          spanId: 'span1',
          elapsedTime: 100,
        } as Transaction,
      ];

      act(() => {
        result.current[1]({
          complete: true,
          resultFrom: 0,
          metadata: mockData,
        });
      });

      expect(result.current[0]).toEqual({
        complete: true,
        resultFrom: 0,
        metadata: mockData,
      });
    });

    test('should update with partial data', () => {
      const { result } = renderHook(() => useAtom(transactionListDatasAtom));
      const mockData: Transaction[] = [
        {
          agentId: 'agent1',
          applicationId: 'app1',
        } as Transaction,
      ];

      act(() => {
        result.current[1]({
          metadata: mockData,
        });
      });

      expect(result.current[0]).toEqual({
        metadata: mockData,
      });
    });

    test('should reset to undefined', () => {
      const { result } = renderHook(() => useAtom(transactionListDatasAtom));
      const mockData: Transaction[] = [
        {
          agentId: 'agent1',
          applicationId: 'app1',
        } as Transaction,
      ];

      act(() => {
        result.current[1]({
          metadata: mockData,
        });
        result.current[1](undefined);
      });

      expect(result.current[0]).toBeUndefined();
    });
  });

  describe('Test "transactionInfoDatasAtom"', () => {
    test('should initialize with null', () => {
      const { result } = renderHook(() => useAtom(transactionInfoDatasAtom));
      expect(result.current[0]).toBeNull();
    });

    test('should update with transaction info data', () => {
      const { result } = renderHook(() => useAtom(transactionInfoDatasAtom));
      const mockData: TransactionInfo.Response = {
        agentId: 'agent1',
        applicationId: 'app1',
        traceId: 'trace1',
      } as TransactionInfo.Response;

      act(() => {
        result.current[1](mockData);
      });

      expect(result.current[0]).toEqual(mockData);
    });

    test('should reset to null', () => {
      const { result } = renderHook(() => useAtom(transactionInfoDatasAtom));
      const mockData: TransactionInfo.Response = {
        agentId: 'agent1',
        applicationId: 'app1',
      } as TransactionInfo.Response;

      act(() => {
        result.current[1](mockData);
        result.current[1](null);
      });

      expect(result.current[0]).toBeNull();
    });
  });

  describe('Test "transactionInfoCurrentTabId"', () => {
    test('should initialize with empty string', () => {
      const { result } = renderHook(() => useAtom(transactionInfoCurrentTabId));
      expect(result.current[0]).toBe('');
    });

    test('should update with tab id', () => {
      const { result } = renderHook(() => useAtom(transactionInfoCurrentTabId));

      act(() => {
        result.current[1]('tab-1');
      });

      expect(result.current[0]).toBe('tab-1');
    });

    test('should update with different tab id', () => {
      const { result } = renderHook(() => useAtom(transactionInfoCurrentTabId));

      act(() => {
        result.current[1]('tab-1');
        result.current[1]('tab-2');
      });

      expect(result.current[0]).toBe('tab-2');
    });
  });

  describe('Test "transactionInfoCallTreeFocusId"', () => {
    test('should initialize with empty string', () => {
      const { result } = renderHook(() => useAtom(transactionInfoCallTreeFocusId));
      expect(result.current[0]).toBe('');
    });

    test('should update with focus id', () => {
      const { result } = renderHook(() => useAtom(transactionInfoCallTreeFocusId));

      act(() => {
        result.current[1]('focus-1');
      });

      expect(result.current[0]).toBe('focus-1');
    });

    test('should update with different focus id', () => {
      const { result } = renderHook(() => useAtom(transactionInfoCallTreeFocusId));

      act(() => {
        result.current[1]('focus-1');
        result.current[1]('focus-2');
      });

      expect(result.current[0]).toBe('focus-2');
    });

    test('should reset to empty string', () => {
      const { result } = renderHook(() => useAtom(transactionInfoCallTreeFocusId));

      act(() => {
        result.current[1]('focus-1');
        result.current[1]('');
      });

      expect(result.current[0]).toBe('');
    });
  });
});

