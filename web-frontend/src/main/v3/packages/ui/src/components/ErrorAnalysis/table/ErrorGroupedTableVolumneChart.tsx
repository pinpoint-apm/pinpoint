import { ErrorAnalysisGroupedErrorList } from '@pinpoint-fe/ui/constants';
import { ComposedChart, ReferenceLine } from 'recharts';
import { useRechart } from '../../ReChart/useRechart';

export interface ErrorGroupedTableVolumneChart {
  chart: ErrorAnalysisGroupedErrorList.ErrorData['chart'];
}

export const ErrorGroupedTableVolumneChart = ({ chart }: ErrorGroupedTableVolumneChart) => {
  const { data, maxValue, renderChartChildComponents } = useRechart(chart);

  return (
    <ComposedChart
      width={128}
      height={40}
      data={data}
      margin={{
        top: 6,
        right: 30,
        bottom: 2,
        left: 2,
      }}
    >
      {data?.length && maxValue > -1 && (
        <ReferenceLine
          y={maxValue}
          stroke="black"
          strokeDasharray="3 3"
          position="middle"
          label={{ position: 'right', value: maxValue }}
          ifOverflow="extendDomain"
        />
      )}
      {renderChartChildComponents()}
    </ComposedChart>
  );
};
