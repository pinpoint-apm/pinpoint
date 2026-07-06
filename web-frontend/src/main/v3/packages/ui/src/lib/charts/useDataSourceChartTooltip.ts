import { InspectorAgentDataSourceChart } from '@pinpoint-fe/ui/src/constants';
import { escapeHTMLEntities } from '@pinpoint-fe/ui/src/utils';

export type DataSourceTooltipValues = {
  jdbcUrl?: string;
  serviceType?: string;
  activeAvg?: number;
  activeMax?: number;
  totalMax?: number;
};

export type DataSourceTooltipRow = {
  name: string;
  color?: string;
  values: DataSourceTooltipValues;
};

export const useDataSourceChartTooltip = (
  tooltipData: InspectorAgentDataSourceChart.MetricValueGroup[] = [],
) => {
  const tooltipTitleList = ['Jdbc URL', 'ServiceType', 'Active Avg', 'Active Max', 'Total Max'];

  const getTooltipData = (focusIndex: number): DataSourceTooltipValues[] =>
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

  const getTooltipStr = (titleList: string[], rows: DataSourceTooltipRow[]) => {
    const header = titleList.map((title) => `<th>${escapeHTMLEntities(title)}</th>`).join('');
    const body = rows
      .map(({ name, color, values }) => {
        const cells = [
          values.jdbcUrl,
          values.serviceType,
          values.activeAvg,
          values.activeMax,
          values.totalMax,
        ]
          .map((value) => `<td class="value">${escapeHTMLEntities(String(value ?? ''))}</td>`)
          .join('');
        return `<tr><td class="name"><span class="swatch" style="background-color:${
          color ?? ''
        };"></span>${escapeHTMLEntities(name)}</td>${cells}</tr>`;
      })
      .join('');
    // table-layout:fixed + colgroup 으로 값 길이와 무관하게 컬럼 폭을 고정한다(긴 값은 셀 안에서 줄바꿈).
    return `<style>
      .ds-tt{width:100%;border-collapse:collapse;table-layout:fixed;font-size:12px;line-height:1.5;box-shadow:none;}
      .ds-tt th,.ds-tt td{border:1px solid #e5e7eb;padding:2px 6px;text-align:left;overflow:hidden;vertical-align:middle;}
      .ds-tt thead th{background-color:#a9a9a9;color:#fff;font-weight:600;white-space:nowrap;border-color:#a9a9a9;}
      .ds-tt tbody td{color:#374151;}
      .ds-tt td.name{white-space:nowrap;overflow:hidden;text-overflow:ellipsis;font-weight:500;}
      .ds-tt td.value{word-break:break-all;white-space:normal;}
      .ds-tt .swatch{display:inline-block;width:10px;height:10px;margin-right:5px;vertical-align:middle;border-radius:1px;}
    </style>
    <table class="ds-tt">
      <colgroup><col style="width:14%"/><col style="width:30%"/><col style="width:14%"/><col style="width:18%"/><col style="width:11%"/><col style="width:13%"/></colgroup>
      <thead><tr>${header}</tr></thead>
      <tbody>${body}</tbody>
    </table>`;
  };

  return { getTooltipData, getTooltipStr, tooltipTitleList };
};
