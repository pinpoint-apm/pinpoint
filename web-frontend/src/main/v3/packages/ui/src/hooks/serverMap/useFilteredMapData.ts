import React from 'react';
import { useAtomValue, useSetAtom } from 'jotai';
import { scatterDataByApplicationKeyAtom } from '@pinpoint-fe/ui/src/atoms/scatter';
import { serverMapDataAtom } from '@pinpoint-fe/ui/src/atoms/serverMap';
import { FilteredMapType as FilteredMap } from '@pinpoint-fe/ui/src/constants';
import {
  mergeFilteredMapLinkData,
  mergeFilteredMapNodeData,
} from '@pinpoint-fe/ui/src/utils/helper/filteredMap/merge';
import { toBasicISOStringMs } from '@pinpoint-fe/ui/src/utils/date';
import { useGetFilteredServerMapData } from '@pinpoint-fe/ui/src/hooks/api/useGetFilteredServerMapData';
import { useFilteredMapParameters } from '@pinpoint-fe/ui/src/hooks/searchParameters/useFilteredMapParameters';
import { useUpdateEffect } from '@pinpoint-fe/ui/src/hooks/utility/useUpdateEffect';

const mergeServerMapData = (
  prev: FilteredMap.Response,
  data: FilteredMap.Response,
): FilteredMap.Response => {
  const timestampArray = [...prev.applicationMapData.timestamp];
  const nodeDataArray = [...prev.applicationMapData.nodeDataArray] as FilteredMap.NodeData[];
  const linkDataArray = [...prev.applicationMapData.linkDataArray] as FilteredMap.LinkData[];

  data?.applicationMapData?.timestamp?.forEach((timestamp) => {
    if (!timestampArray.includes(timestamp)) {
      timestampArray.push(timestamp);
    }
  });

  data?.applicationMapData?.nodeDataArray?.forEach((newNodeData) => {
    const existingIndex = nodeDataArray.findIndex(
      (prevNodeData) => newNodeData.key === prevNodeData.key,
    );

    if (existingIndex !== -1) {
      nodeDataArray[existingIndex] = mergeFilteredMapNodeData(
        {
          timestamp: prev?.applicationMapData?.timestamp,
          data: nodeDataArray[existingIndex],
        },
        {
          timestamp: data?.applicationMapData?.timestamp,
          data: newNodeData,
        },
      );
    } else {
      nodeDataArray.push(newNodeData);
    }
  });

  data?.applicationMapData?.linkDataArray?.forEach((newLinkData) => {
    const existingIndex = linkDataArray.findIndex(
      (prevLinkData) => prevLinkData.key === newLinkData.key,
    );

    if (existingIndex !== -1) {
      linkDataArray[existingIndex] = mergeFilteredMapLinkData(
        {
          timestamp: prev?.applicationMapData?.timestamp,
          data: linkDataArray[existingIndex],
        },
        {
          timestamp: data?.applicationMapData?.timestamp,
          data: newLinkData,
        },
      );
    } else {
      linkDataArray.push(newLinkData);
    }
  });

  return {
    ...data,
    applicationMapData: {
      ...data?.applicationMapData,
      timestamp: timestampArray,
      nodeDataArray,
      linkDataArray,
    },
  };
};

/**
 * filteredMap의 조회 결과를 쌓는 곳. **쌓기와 비우기를 이 훅 한 곳에서 한다.**
 *
 * filteredMap은 기간을 뒤에서부터 조금씩 거슬러 불러오는 화면이라, 응답이 올 때마다 이전 값과
 * 합친다. 그 누적값은 React Query 캐시에 없는 상태다(캐시에는 응답 한 조각씩만 있다).
 * 예전에는 쌓기는 `FilteredMapFetcher`(자식), 비우기는 `FilteredMapPage`(부모)에 있어서 한쪽만
 * 고치면 조용히 어긋났다. (이슈 #10603)
 *
 * - **화면에 들어올 때 비운다.** 아톰은 remount로 지워지지 않는다. 같은 탭에서 servermap으로 갔다가
 *   뒤로가기로 돌아오면 아톰에는 servermap의 map이 들어 있고, 비우지 않으면 첫 응답이 그 위에 합쳐졌다.
 * - **조회 조건(query string)이 바뀌면 비운다.** 기간·필터가 바뀌면 처음부터 다시 쌓는다.
 *   (`useGetFilteredServerMapData`도 같은 시점에 조회 파라미터를 처음으로 되돌린다.)
 * - **화면을 떠날 때 스캐터를 비운다.** 스캐터 아톰도 쓸 때마다 합치는 누적기다.
 *
 * 반환하는 `data`는 **방금 도착한 응답 한 조각**이고, `serverMapData`가 지금까지 쌓은 값이다.
 */
export const useFilteredMapData = (isPaused: boolean) => {
  const serverMapData = useAtomValue(serverMapDataAtom);
  const setServerMapData = useSetAtom(serverMapDataAtom);
  const setScatterDataByApplicationKey = useSetAtom(scatterDataByApplicationKeyAtom);
  const { dateRange, search } = useFilteredMapParameters();
  const from = dateRange.from.getTime();
  const { data, error, isLoading, setQueryParams } = useGetFilteredServerMapData(isPaused);

  // 아래 응답 effect보다 먼저 선언해야 한다. 캐시에 있던 응답으로 마운트되면 같은 commit에서
  // 응답 effect도 돌므로, 비운 뒤에 쌓아야 한다. (setter는 store가 같으면 바뀌지 않으므로
  // 마운트 때 한 번만 돈다.)
  React.useEffect(() => {
    setServerMapData(undefined);
    setScatterDataByApplicationKey(undefined);
    return () => {
      setScatterDataByApplicationKey(undefined);
    };
  }, [setServerMapData, setScatterDataByApplicationKey]);

  useUpdateEffect(() => {
    setServerMapData(undefined);
    setScatterDataByApplicationKey(undefined);
  }, [search]);

  React.useEffect(() => {
    if (!isLoading && data) {
      setServerMapData((prev) =>
        prev ? mergeServerMapData(prev as FilteredMap.Response, data) : data,
      );

      setScatterDataByApplicationKey(data.applicationScatterData);

      if (data?.lastFetchedTimestamp > from) {
        setQueryParams((prev) => ({
          ...prev,
          to: toBasicISOStringMs(new Date(data.lastFetchedTimestamp - 1)),
        }));
      }
    }
  }, [data]);

  return { serverMapData, data, error };
};
