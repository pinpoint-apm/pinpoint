import { PrimitiveArray, Data } from 'billboard.js';
import { Observable } from 'rxjs';

import { IInspectorChartContainer } from './inspector-chart-container-factory';
import { makeYData, makeXData, getMaxTickValue } from 'app/core/utils/chart-util';
import { IInspectorChartData, InspectorChartDataService } from './inspector-chart-data.service';

export class AgentDirectBufferCountChartContainer implements IInspectorChartContainer {
    private apiUrl = 'getAgentStat/directBuffer/chart.pinpoint';

    defaultYMax = 100;
    title = 'Direct Buffer Count';

    constructor(
        private inspectorChartDataService: InspectorChartDataService
    ) {}

    getData(range: number[]): Observable<IInspectorChartData | AjaxException> {
        return this.inspectorChartDataService.getData(this.apiUrl, range);
    }

    makeChartData({charts}: IInspectorChartData): PrimitiveArray[] {
        return [
            ['x', ...makeXData(charts.x)],
            ['directCount', ...makeYData(charts.y['DIRECT_COUNT'], 2)],
        ];
    }

    makeDataOption(): Data {
        return {
            type: 'spline',
            names: {
                directCount: 'Direct Buffer Count',
            },
            colors: {
                directCount: 'rgba(31, 119, 180, 0.4)',
            }
        };
    }

    makeElseOption(): {[key: string]: any} {
        return {};
    }

    makeYAxisOptions(data: PrimitiveArray[]): {[key: string]: any} {
        return {
            y: {
                label: {
                    text: 'Buffer (count)',
                    position: 'outer-middle'
                },
                tick: {
                    count: 5,
                    format: (v: number): string => this.convertWithUnit(v)
                },
                padding: {
                    top: 0,
                    bottom: 0
                },
                min: 0,
                max: (() => {
                    const maxTickValue = getMaxTickValue(data, 1);

                    return maxTickValue === 0 ? this.defaultYMax : maxTickValue;
                })(),
                default: [0, this.defaultYMax]
            }
        };
    }

    convertWithUnit(value: number): string {
        return value.toString();
    }

    getTooltipFormat(v: number, columnId: string, i: number): string {
        return Number.isInteger(v) ? v.toString() : v.toFixed(2);
    }
}
