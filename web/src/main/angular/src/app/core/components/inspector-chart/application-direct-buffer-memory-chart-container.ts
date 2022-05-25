import { PrimitiveArray, Data, spline } from 'billboard.js';
import { Observable } from 'rxjs';

import { IInspectorChartContainer } from './inspector-chart-container-factory';
import { makeYData, makeXData, getMaxTickValue } from 'app/core/utils/chart-util';
import { IInspectorChartData, InspectorChartDataService } from './inspector-chart-data.service';
import { getAgentId } from './inspector-chart-util';
import { InspectorChartThemeService } from './inspector-chart-theme.service';

export class ApplicationDirectBufferMemoryChartContainer implements IInspectorChartContainer {
    private apiUrl = 'getApplicationStat/directBuffer/chart.pinpoint';
    private minAgentIdList: string[];
    private maxAgentIdList: string[];

    defaultYMax = 100;
    title = 'Direct Buffer Memory';

    constructor(
        private inspectorChartDataService: InspectorChartDataService,
        private inspectorChartThemeService: InspectorChartThemeService,
    ) {}

    getData(range: number[]): Observable<IInspectorChartData> {
        return this.inspectorChartDataService.getData(this.apiUrl, range);
    }

    makeChartData({charts}: IInspectorChartData): PrimitiveArray[] {
        this.minAgentIdList = makeYData(charts.y['DIRECT_MEMORY_USED'], 1) as string[];
        this.maxAgentIdList = makeYData(charts.y['DIRECT_MEMORY_USED'], 3) as string[];

        return [
            ['x', ...makeXData(charts.x)],
            ['max', ...makeYData(charts.y['DIRECT_MEMORY_USED'], 2)],
            ['avg', ...makeYData(charts.y['DIRECT_MEMORY_USED'], 4)],
            ['min', ...makeYData(charts.y['DIRECT_MEMORY_USED'], 0)],
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
                ...this.inspectorChartThemeService.getMinAvgMaxColors()
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

    makeTooltipOptions(): {[key: string]: any} {
        return {};
    }

    convertWithUnit(value: number): string {
        const unitList = ['', 'K', 'M', 'G'];

        return [...unitList].reduce((acc: string, curr: string, i: number, arr: string[]) => {
            const v = Number(acc);

            return v >= 1024
                ? (v / 1024).toString()
                : (arr.splice(i + 1), Number.isInteger(v) ? `${v}${curr}` : `${v.toFixed(2)}${curr}`);
        }, value.toString());
    }

    getTooltipFormat(v: number, columnId: string, i: number): string {
        return `${this.convertWithUnit(v)} ${getAgentId(columnId, i, this.minAgentIdList, this.maxAgentIdList)}`;
    }
}
