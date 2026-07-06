import React from 'react';
import { useGetInspectorApplicationDataSourceChartData } from '@pinpoint-fe/ui/src/hooks';
import { InspectorChart } from '../InspectorChart';
import { useChartConfig } from '../../../../lib';
import { ApplicationDataSourceChartTable, ApplicationDataSourceChartTableData } from '.';
import { colors } from '@pinpoint-fe/ui/src/constants';

export interface ApplicationDataSourceChartProps {
  className?: string;
  emptyMessage?: string;
}

export const ApplicationDataSourceChart = ({ ...props }: ApplicationDataSourceChartProps) => {
  const { data } = useGetInspectorApplicationDataSourceChartData({
    metricDefinitionId: 'dataSource',
  });
  const [selectedChartIndex, setSelectedChartIndex] = React.useState<number>(0);
  const chartConfig = useChartConfig(
    data
      ? {
          ...data,
          metricValues: data.metricValueGroups[selectedChartIndex]?.metricValues,
        }
      : undefined,
    {
      colors: {
        AVG: colors.violet[800],
        MIN: colors.sky[500],
        MAX: colors.blue[800],
      },
      dashedFields: ['MIN', 'MAX'],
    },
  );
  const tableData = data?.metricValueGroups.map(({ tags }) => {
    return tags.reduce((acc, { name, value }) => {
      return { ...acc, [name]: value };
    }, {} as ApplicationDataSourceChartTableData);
  });

  return (
    chartConfig && (
      <InspectorChart
        data={chartConfig.chartData}
        chartOptions={chartConfig.chartOptions}
        {...props}
      >
        <ApplicationDataSourceChartTable
          className="mt-3"
          data={tableData}
          onClickRow={({ index }) => {
            setSelectedChartIndex(index);
          }}
          selectedRowIndex={selectedChartIndex}
        />
      </InspectorChart>
    )
  );
};
