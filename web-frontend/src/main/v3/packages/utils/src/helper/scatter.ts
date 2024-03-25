import {
  FilteredMap,
  GetScatter,
  SCATTER_DATA_TOTAL_KEY,
  ScatterDataByAgent,
} from '@pinpoint-fe/constants';

export const getScatterChartedData = (dotList: number[], from: number) => {
  return {
    x: from + dotList[0],
    y: dotList[1],
    type: dotList[4] === 1 ? 'success' : 'failed',
    hidden: dotList[5] === 0,
  };
};

export const getScatterData = (
  newData: GetScatter.Response | FilteredMap.ScatterData,
  prevData?: ScatterDataByAgent,
) => {
  const metadata = newData?.scatter?.metadata;
  const result = newData?.scatter?.dotList?.reduce<ScatterDataByAgent>(
    (prev, dot, i) => {
      const agentName = metadata[dot[2]][0];
      const scatterData = getScatterChartedData(dot, newData.from);

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

  return result;
};
