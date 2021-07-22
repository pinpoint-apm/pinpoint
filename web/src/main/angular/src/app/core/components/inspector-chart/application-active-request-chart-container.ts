import { PrimitiveArray, Data, spline } from 'billboard.js';
import { Observable } from 'rxjs';

import { IInspectorChartContainer } from './inspector-chart-container-factory';
import { makeYData, makeXData, getMaxTickValue } from 'app/core/utils/chart-util';
import { IInspectorChartData, InspectorChartDataService } from './inspector-chart-data.service';
import { getAgentId } from './inspector-chart-util';

export class ApplicationActiveRequestChartContainer implements IInspectorChartContainer {
    private apiUrl = 'getApplicationStat/activeTrace/chart.pinpoint';
    private minAgentIdList: string[];
    private maxAgentIdList: string[];

    defaultYMax = 4;
    title = 'Active Request';

    constructor(
        private inspectorChartDataService: InspectorChartDataService
    ) {}

    getData(range: number[]): Observable<IInspectorChartData> {
        return this.inspectorChartDataService.getData(this.apiUrl, range);
    }

    makeChartData({charts}: IInspectorChartData): PrimitiveArray[] {
        this.minAgentIdList = makeYData(charts.y['ACTIVE_TRACE_COUNT'], 1) as string[];
        this.maxAgentIdList = makeYData(charts.y['ACTIVE_TRACE_COUNT'], 3) as string[];

        return [
            ['x', ...makeXData(charts.x)],
            ['max', ...makeYData(charts.y['ACTIVE_TRACE_COUNT'], 2)],
            ['avg', ...makeYData(charts.y['ACTIVE_TRACE_COUNT'], 4)],
            ['min', ...makeYData(charts.y['ACTIVE_TRACE_COUNT'], 0)],
        ];
    }

    makeDataOption(): Data {
        return {
            type: spline(),
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
                    text: 'Active Request (count)',
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
        const unitList = ['', 'K', 'M', 'G'];

        return [...unitList].reduce((acc: string, curr: string, i: number, arr: string[]) => {
            const v = Number(acc);

            return v >= 1000
                ? (v / 1000).toString()
                : (arr.splice(i + 1), Number.isInteger(v) ? `${v}${curr}` : `${v.toFixed(2)}${curr}`);
        }, value.toString());
    }

    getTooltipFormat(v: number, columnId: string, i: number): string {
        return `${this.convertWithUnit(v)} ${getAgentId(columnId, i, this.minAgentIdList, this.maxAgentIdList)}`;
    }
}
