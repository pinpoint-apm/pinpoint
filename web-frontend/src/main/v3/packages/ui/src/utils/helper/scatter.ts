import {
  FilteredMapType as FilteredMap,
  GetScatter,
  SCATTER_DATA_TOTAL_KEY,
  ScatterDataByAgent,
} from '@pinpoint-fe/ui/src/constants';

const getScatterChartedData = (dotList: number[], from: number) => {
  return {
    x: from + dotList[0],
    y: dotList[1],
    type: dotList[4] === 1 ? 'success' : 'failed',
    hidden: dotList[5] === 0,
  };
};

const getFilterMapScatterChartedData = (
  dotList: number[],
  from: number,
  transactionId: number,
  collectorAcceptTime: number,
  agentId: string,
) => {
  return {
    ...getScatterChartedData(dotList, from),
    transactionId,
    collectorAcceptTime,
    agentId,
  };
};

export const getScatterData = (
  newData: GetScatter.Response | FilteredMap.ScatterData,
  prevData?: ScatterDataByAgent,
  // this for filtermap
  option?: { isFilterMap?: boolean },
) => {
  const metadata = newData?.scatter?.metadata;
  const result = newData?.scatter?.dotList?.reduce<ScatterDataByAgent>(
    (prev, dot, i) => {
      const agentMetaData = metadata[dot[2]];
      const agentName = agentMetaData[0];
      const scatterData = option?.isFilterMap
        ? getFilterMapScatterChartedData(
            dot,
            newData.from,
            dot[3],
            agentMetaData[2],
            agentMetaData[1],
          )
        : getScatterChartedData(dot, newData.from);

      // 한 틱 단위 ex) 5000개 에 대한 정보
      if (i === 0) {
        prev.curr = { [SCATTER_DATA_TOTAL_KEY]: [] };
      }
      prev.curr[SCATTER_DATA_TOTAL_KEY]?.push(scatterData);
      if (prev.curr[agentName]) {
        prev.curr[agentName]?.push(scatterData);
      } else {
        prev.curr[agentName] = [scatterData];
      }

      // 누적 정보
      prev.acc[SCATTER_DATA_TOTAL_KEY]?.push(scatterData);
      if (prev.acc[agentName]) {
        prev.acc[agentName]?.push(scatterData);
      } else {
        prev.acc[agentName] = [scatterData];
      }
      return prev;
    },
    prevData || {
      curr: {
        [SCATTER_DATA_TOTAL_KEY]: [],
      },
      acc: {
        [SCATTER_DATA_TOTAL_KEY]: [],
      },
    },
  );

  // 깊은 복사 또는 얕은 복사를 통해 새로운 객체 참조 생성
  return {
    curr: { ...result.curr },
    acc: { ...result.acc },
    dateRange: (newData as GetScatter.Response)?.complete
      ? ([newData?.from, newData?.to] as ScatterDataByAgent['dateRange'])
      : undefined,
  };
};
