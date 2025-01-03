import { InspectorAgentChart, InspectorApplicationChart } from '@pinpoint-fe/ui/constants';
import { getFormat } from '@pinpoint-fe/ui/utils';

export const TOOLTIP_NOT_LINKED_CHART_LIST = ['Apdex Score'];

export const useChartTooltip = (
  tooltipData: InspectorAgentChart.MetricValue[] | InspectorApplicationChart.MetricValue[],
) => {
  const getTooltipData = (dataIndex: number) => {
    return tooltipData.map(({ fieldName, unit, valueList }) => {
      return {
        color: '#f87171', // TODO: Should use server-data color,
        id: fieldName,
        value: getFormat(unit)(valueList[dataIndex]),
      };
    });
  };

  const getTooltipStr = (
    title: string,
    contentsData: { id: string; value: number | string; color: string }[],
  ) => {
    const header = `<tr><th colspan="2">${title}</th></tr>`;
    const body = contentsData
      .map((d) => {
        const { id, value, color } = d;

        return `
          <tr class="bb-tooltip-name-${id}">
            <td class="name"><span style="background-color:${color}"></span>${id}</td>
            <td class="value">${value}</td>
          </tr>
        `;
      })
      .join('');

    return `<table class="bb-tooltip"><tbody>${header}${body}</tbody></table>`;
  };

  return { getTooltipData, getTooltipStr };
};
