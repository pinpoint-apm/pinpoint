import { PrimitiveArray, Data, spline, areaSpline } from 'billboard.js';
import { Observable } from 'rxjs';

import { IInspectorChartContainer } from './inspector-chart-container-factory';
import { makeYData, makeXData, getMaxTickValue } from 'app/core/utils/chart-util';
import { IInspectorChartData, InspectorChartDataService } from './inspector-chart-data.service';
import { InspectorChartThemeService } from './inspector-chart-theme.service';

export class AgentResponseTimeChartContainer implements IInspectorChartContainer {
    private apiUrl = 'getAgentStat/responseTime/chart.pinpoint';

    defaultYMax = 100;
    title = 'Response Time';

    constructor(
        private inspectorChartDataService: InspectorChartDataService,
        private inspectorChartThemeService: InspectorChartThemeService,
    ) {}

    getData(range: number[]): Observable<IInspectorChartData> {
        return this.inspectorChartDataService.getData(this.apiUrl, range);
    }

    makeChartData({charts}: IInspectorChartData): PrimitiveArray[] {
        return [
            ['x', ...makeXData(charts.x)],
            ['max', ...makeYData(charts.y['MAX'], 1)],
            ['avg', ...makeYData(charts.y['AVG'], 2)],
        ];
    }

    makeDataOption(): Data {
        const alpha = this.inspectorChartThemeService.getAlpha(0.4);
        
        return {
            types: {
                avg: areaSpline(),
                max: spline()
            },
            names: {
                avg: 'Avg',
                max: 'Max'
            },
            colors: {
                avg: `rgba(44, 160, 44, ${alpha})`,
                max: `rgba(246, 145, 36, ${alpha})`
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
                    text: 'Response Time (ms)',
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

    makeTooltipOptions(): {[key: string]: any} {
        return {};
    }

    convertWithUnit(value: number): string {
        const unitList = ['ms', 'sec'];

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
