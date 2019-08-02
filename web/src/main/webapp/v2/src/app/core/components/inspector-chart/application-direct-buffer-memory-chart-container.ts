import { PrimitiveArray, Data } from 'billboard.js';
import { Observable } from 'rxjs';

import { IInspectorChartContainer } from './inspector-chart-container-factory';
import { makeYData, makeXData, getMaxTickValue } from './inspector-chart-util';
import { IInspectorChartData, InspectorChartDataService } from './inspector-chart-data.service';

export class ApplicationDirectBufferMemoryChartContainer implements IInspectorChartContainer {
    private apiUrl = 'getApplicationStat/directBuffer/chart.pinpoint';
    defaultYMax = 100;
    title = 'Direct Buffer Memory';

    constructor(
        private inspectorChartDataService: InspectorChartDataService
    ) {}

    getData(range: number[]): Observable<IInspectorChartData | AjaxException> {
        return this.inspectorChartDataService.getData(this.apiUrl, range);
    }

    makeChartData({charts}: IInspectorChartData): PrimitiveArray[] {
        return [
            ['x', ...makeXData(charts.x)],
            ['min', ...makeYData(charts.y['DIRECT_MEMORY_USED'], 0)],
            ['avg', ...makeYData(charts.y['DIRECT_MEMORY_USED'], 4)],
            ['max', ...makeYData(charts.y['DIRECT_MEMORY_USED'], 2)],
        ];
    }

    makeDataOption(): Data {
        return {
            type: 'spline',
            names: {
                min: 'Min',
                avg: 'Avg',
                max: 'Max',
            },
            colors: {
                min: '#66B2FF',
                avg: '#4C0099',
                max: '#0000CC',
            }
        };
    }

    makeElseOption(): {[key: string]: any} {
        return {
            line: {
                classes: ['min', 'avg', 'max']
            }
        };
    }

    makeYAxisOptions(data: PrimitiveArray[]): {[key: string]: any} {
        return {
            y: {
                label: {
                    text: 'Memory (bytes)',
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

    makeMinAgentIdList({charts}: IInspectorChartData): string[] {
        return makeYData(charts.y['DIRECT_MEMORY_USED'], 1) as string[];
    }

    makeMaxAgentIdList({charts}: IInspectorChartData): string[] {
        return makeYData(charts.y['DIRECT_MEMORY_USED'], 3) as string[];
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
}
