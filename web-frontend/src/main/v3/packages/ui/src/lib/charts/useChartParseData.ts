import { InspectorAgentChart } from '@pinpoint-fe/ui/constants';

const STACKING_DATA_FIELDNAME_LIST = [
  'fastCount',
  'normalCount',
  'slowCount',
  'verySlowCount',
  'sampledNewCount',
  'sampledContinuationCount',
  'unsampledNewCount',
  'unsampledContinuationCount',
  'skippedNewSkipCount',
  'skippedContinuationCount',
]; // TODO: Should remove when the server passes the info

export const useChartParseData = (data: InspectorAgentChart.MetricValue[] = []) => {
  const dataKeys: string[] = [];
  const { chartMetricData, chartTooltipData } = data.reduce<{
    chartMetricData: InspectorAgentChart.MetricValue[];
    chartTooltipData: InspectorAgentChart.MetricValue[];
  }>(
    (acc, curr) => {
      STACKING_DATA_FIELDNAME_LIST.includes(curr.fieldName) && dataKeys.push(curr.fieldName);
      curr.chartType === 'tooltip'
        ? acc.chartTooltipData.push(curr)
        : acc.chartMetricData.push(curr);

      return acc;
    },
    { chartMetricData: [], chartTooltipData: [] },
  );

  return { chartMetricData, chartTooltipData, dataKeys };
};
