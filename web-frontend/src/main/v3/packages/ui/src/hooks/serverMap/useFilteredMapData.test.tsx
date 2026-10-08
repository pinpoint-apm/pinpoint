import { act, renderHook } from '@testing-library/react';
import { getDefaultStore } from 'jotai';
import { scatterDataByApplicationKeyAtom } from '@pinpoint-fe/ui/src/atoms/scatter';
import { serverMapDataAtom } from '@pinpoint-fe/ui/src/atoms/serverMap';
import { FilteredMapType as FilteredMap, GetServerMap } from '@pinpoint-fe/ui/src/constants';
import { useGetFilteredServerMapData } from '@pinpoint-fe/ui/src/hooks/api/useGetFilteredServerMapData';
import { useFilteredMapParameters } from '@pinpoint-fe/ui/src/hooks/searchParameters/useFilteredMapParameters';
import {
  newLink,
  newNode,
  nextTimestamp,
  prevLink,
  prevNode,
  prevTimestamp,
  resultLink,
  resultNode,
} from '@pinpoint-fe/ui/src/utils/helper/filteredMap/mergeMock';
import { useFilteredMapData } from './useFilteredMapData';

jest.mock('@pinpoint-fe/ui/src/hooks/api/useGetFilteredServerMapData', () => ({
  useGetFilteredServerMapData: jest.fn(),
}));
jest.mock('@pinpoint-fe/ui/src/hooks/searchParameters/useFilteredMapParameters', () => ({
  useFilteredMapParameters: jest.fn(),
}));

const store = getDefaultStore();
const FROM = new Date('2026-10-08T10:00:00Z').getTime();
const TO = new Date('2026-10-08T10:20:00Z').getTime();

// merge 함수들은 누적값을 제자리에서 고친다. 테스트끼리 fixture를 공유하지 않도록 매번 복사한다.
const clone = <T,>(value: T): T => JSON.parse(JSON.stringify(value));

const response = ({
  timestamp,
  nodes = [],
  links = [],
  lastFetchedTimestamp = FROM,
  scatterKey = 'application^UNAUTHORIZED',
}: {
  timestamp: number[];
  nodes?: FilteredMap.NodeData[];
  links?: FilteredMap.LinkData[];
  lastFetchedTimestamp?: number;
  scatterKey?: string;
}) =>
  ({
    applicationMapData: {
      range: { from: FROM, to: TO },
      timestamp: [...timestamp],
      nodeDataArray: clone(nodes),
      linkDataArray: clone(links),
    },
    lastFetchedTimestamp,
    applicationScatterData: {
      [scatterKey]: { from: FROM, to: TO, resultFrom: FROM, currentServerTime: TO, dotList: [] },
    },
  }) as unknown as FilteredMap.Response;

const setQueryParams = jest.fn();
let query: { data?: FilteredMap.Response; isLoading: boolean; error: Error | null };
let search: string;

/** 응답이 새로 도착한 것처럼 만든다. */
const arrive = (data: FilteredMap.Response, rerender: () => void) => {
  query = { ...query, data };
  rerender();
};

const accumulated = () => store.get(serverMapDataAtom) as FilteredMap.Response | undefined;

describe('useFilteredMapData', () => {
  beforeEach(() => {
    query = { data: undefined, isLoading: false, error: null };
    search = '?from=a&to=b&filter=f';
    (useGetFilteredServerMapData as jest.Mock).mockImplementation(() => ({
      ...query,
      setQueryParams,
    }));
    (useFilteredMapParameters as jest.Mock).mockImplementation(() => ({
      dateRange: { from: new Date(FROM), to: new Date(TO) },
      search,
    }));
    act(() => {
      store.set(serverMapDataAtom, undefined);
      store.set(scatterDataByApplicationKeyAtom, undefined);
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test('stores the first response as it is', () => {
    const { result, rerender } = renderHook(() => useFilteredMapData(false));
    const first = response({ timestamp: prevTimestamp, nodes: [prevNode] });

    arrive(first, rerender);

    expect(accumulated()).toBe(first);
    expect(result.current.serverMapData).toBe(first);
    expect(store.get(scatterDataByApplicationKeyAtom)).toHaveProperty(['application^UNAUTHORIZED']);
  });

  // filteredMap은 기간을 거슬러 조금씩 불러온다. 같은 key의 노드·링크는 합치고, 새 것은 덧붙인다.
  test('merges every later response into what was gathered so far', () => {
    const { rerender } = renderHook(() => useFilteredMapData(false));
    const otherNode = { ...clone(newNode), key: 'other-key' };

    arrive(response({ timestamp: prevTimestamp, nodes: [prevNode], links: [prevLink] }), rerender);
    arrive(
      response({ timestamp: nextTimestamp, nodes: [newNode, otherNode], links: [newLink] }),
      rerender,
    );

    const { timestamp, nodeDataArray, linkDataArray } = accumulated()!.applicationMapData;
    expect(timestamp).toEqual([...new Set([...prevTimestamp, ...nextTimestamp])]);
    expect(nodeDataArray).toEqual([resultNode, otherNode]);
    expect(linkDataArray).toEqual([resultLink]);
  });

  test('asks for the earlier slice until the start of the range is reached', () => {
    const { rerender } = renderHook(() => useFilteredMapData(false));
    const lastFetchedTimestamp = FROM + 60_000;

    arrive(response({ timestamp: prevTimestamp, lastFetchedTimestamp }), rerender);

    expect(setQueryParams).toHaveBeenCalledTimes(1);
    const next = setQueryParams.mock.calls[0][0]({ to: 'before' });
    expect(next.to).not.toBe('before');

    arrive(response({ timestamp: nextTimestamp, lastFetchedTimestamp: FROM }), rerender);

    expect(setQueryParams).toHaveBeenCalledTimes(1);
  });

  test('does not gather while the response is still loading', () => {
    const { rerender } = renderHook(() => useFilteredMapData(false));

    query = { ...query, isLoading: true };
    arrive(response({ timestamp: prevTimestamp, nodes: [prevNode] }), rerender);

    expect(accumulated()).toBeUndefined();
  });

  // 기간·필터가 바뀌면 처음부터 다시 쌓는다. 이전 조건의 값 위에 합치면 안 된다.
  test('starts over when the query string changes', () => {
    const { rerender } = renderHook(() => useFilteredMapData(false));
    arrive(response({ timestamp: prevTimestamp, nodes: [prevNode] }), rerender);

    search = '?from=c&to=d&filter=f';
    rerender();

    expect(accumulated()).toBeUndefined();
    expect(store.get(scatterDataByApplicationKeyAtom)).toBeUndefined();

    const fresh = response({ timestamp: nextTimestamp, nodes: [newNode] });
    arrive(fresh, rerender);

    expect(accumulated()).toBe(fresh);
  });

  // 아톰은 remount로 지워지지 않는다. 같은 탭에서 servermap으로 갔다가 뒤로가기로 돌아오면
  // 아톰에 servermap의 map이 들어 있다. 첫 응답을 그 위에 합치면 안 된다.
  test('does not gather on top of a map another screen left in the atom', () => {
    act(() => {
      store.set(serverMapDataAtom, {
        applicationMapData: {
          timestamp: [1],
          nodeDataArray: [{ key: 'servermap-node' }],
          linkDataArray: [],
        },
      } as unknown as GetServerMap.Response);
    });
    const first = response({ timestamp: prevTimestamp, nodes: [prevNode] });
    query = { ...query, data: first };

    renderHook(() => useFilteredMapData(false));

    expect(accumulated()).toBe(first);
  });

  test('clears the scatter it gathered when the screen goes away', () => {
    const { rerender, unmount } = renderHook(() => useFilteredMapData(false));
    arrive(response({ timestamp: prevTimestamp }), rerender);
    expect(store.get(scatterDataByApplicationKeyAtom)).toBeDefined();

    unmount();

    expect(store.get(scatterDataByApplicationKeyAtom)).toBeUndefined();
  });
});
