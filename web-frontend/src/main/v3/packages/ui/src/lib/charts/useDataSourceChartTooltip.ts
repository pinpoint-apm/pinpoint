import { InspectorAgentDataSourceChart } from '@pinpoint-fe/ui/src/constants';

type TooltipContentsData = {
  id: string;
  values: Record<string, unknown>[];
  color: string;
  name?: string;
};

export const useDataSourceChartTooltip = (
  tooltipData: InspectorAgentDataSourceChart.MetricValueGroup[] = [],
) => {
  const tooltipTitleList = ['Jdbc URL', 'ServiceType', 'Active Avg', 'Active Max', 'Total Max'];

  const getTooltipData = (focusIndex: number) =>
    tooltipData.map(({ metricValues, tags }) => {
      const getTagValue = (name: string) =>
        tags.find(({ name: tagName }: InspectorAgentDataSourceChart.TagValue) => tagName === name)
          ?.value;
      const getMetricValue = (name: string) =>
        metricValues.find(
          ({ fieldName }: InspectorAgentDataSourceChart.MetricValue) => fieldName === name,
        )?.valueList[focusIndex];
      return {
        jdbcUrl: getTagValue('jdbcUrl'),
        serviceType: getTagValue('serviceType'),
        activeAvg: getMetricValue('activeAvg'),
        activeMax: getMetricValue('activeMax'),
        totalMax: getMetricValue('totalMax'),
      };
    });

  const getTooltipStr = (titleList: string[], contentsData: TooltipContentsData[]) => {
    const header = titleList
      .map((title) => {
        return `<th>${title}</th>`;
      })
      .join('');
    const body = contentsData
      .map((d, i) => {
        const { id, values, name, color } = d;
        return `
          <tr class="bb-tooltip-name-${id}">
            <td class="name"><span style="background-color:${color}"></span>${name}</td>
            ${Object.values(values[i])
              ?.map(
                (value) =>
                  `<td class="value"><div style="word-break:break-all;white-space:normal;">${value}</div></td>`,
              )
              .join('')}
          </tr>
        `;
      })
      .join('');
    return `<table class="bb-tooltip" style="width:100%;box-shadow:none;">
    <tbody><tr>${header}</tr>${body}</tbody></table>`;
  };

  return { getTooltipData, getTooltipStr, tooltipTitleList };
};
