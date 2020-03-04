import { PrimitiveArray, Data } from 'billboard.js';
import { Observable } from 'rxjs';

import { makeYData, makeXData, getMaxTickValue, getStackedData } from 'app/core/utils/chart-util';
import { IInspectorChartData, InspectorChartDataService } from './inspector-chart-data.service';
import { IInspectorChartContainer } from './inspector-chart-container-factory';

export class AgentActiveThreadChartContainer implements IInspectorChartContainer {
    private apiUrl = 'getAgentStat/activeTrace/chart.pinpoint';

    defaultYMax = 4;
    title = 'Active Thread';

    constructor(
        private inspectorChartDataService: InspectorChartDataService
    ) {}

    getData(range: number[]): Observable<IInspectorChartData | AjaxException> {
        return this.inspectorChartDataService.getData(this.apiUrl, range);
    }

    makeChartData({charts}: IInspectorChartData): PrimitiveArray[] {
        return [
            ['x', ...makeXData(charts.x)],
            ['fast', ...makeYData(charts.y['ACTIVE_TRACE_FAST'], 2)],
            ['normal', ...makeYData(charts.y['ACTIVE_TRACE_NORMAL'], 2)],
            ['slow', ...makeYData(charts.y['ACTIVE_TRACE_SLOW'], 2)],
            ['verySlow', ...makeYData(charts.y['ACTIVE_TRACE_VERY_SLOW'], 2)],
        ];
    }

    makeDataOption(): Data {
        return {
            type: 'area-spline',
            names: {
                fast: 'Fast',
                normal: 'Normal',
                slow: 'Slow',
                verySlow: 'Very Slow'
            },
            colors: {
                fast: 'rgba(44, 160, 44, 0.4)',
                normal: 'rgba(60, 129, 250, 0.4)',
                slow: 'rgba(248, 199, 49, 0.4)',
                verySlow: 'rgba(246, 145, 36, 0.4)'
            },
            groups: [['fast', 'normal', 'slow', 'verySlow']],
            order: null
        };
    }

    makeElseOption(): {[key: string]: any} {
        return {};
    }

    makeYAxisOptions(data: PrimitiveArray[]): {[key: string]: any} {
        return {
            y: {
                label: {
                    text: 'Active Thread (count)',
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
                    const maxTickValue = getMaxTickValue(getStackedData(data), 1);

                    return maxTickValue === 0 ? this.defaultYMax : maxTickValue;
                })(),
                default: [0, this.defaultYMax]
            }
        };
    }

    convertWithUnit(value: number): string {
        const unitList = ['', 'K', 'M', 'G'];

        return [...unitList].reduce((acc: string, curr: string, i: number, arr: string[]) => {
            const v = Number(acc);

            return v >= 1000
                ? (v / 1000).toString()
                : (arr.splice(i + 1), Number.isInteger(v) ? `${v}${curr}` : `${v.toFixed(2)}${curr}`);
        }, value.toString());
    }

    getTooltipFormat(v: number, columnId: string, i: number): string {
        return this.convertWithUnit(v);
    }
}
