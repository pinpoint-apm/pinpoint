import { PrimitiveArray, Data, spline } from 'billboard.js';
import { Observable } from 'rxjs';

import { IInspectorChartContainer } from './inspector-chart-container-factory';
import { makeYData, makeXData, getMaxTickValue } from 'app/core/utils/chart-util';
import { IInspectorChartData, InspectorChartDataService } from './inspector-chart-data.service';
import { getAgentId } from './inspector-chart-util';
import { InspectorChartThemeService } from './inspector-chart-theme.service';

export class ApplicationTotalThreadCountChartContainer implements IInspectorChartContainer {
    private apiUrl = 'getApplicationStat/totalThreadCount/chart.pinpoint';
    private minAgentIdList: string[];
    private maxAgentIdList: string[];

    defaultYMax = 100;
    title = 'Total Thread';

    constructor(
        private inspectorChartDataService: InspectorChartDataService,
        private inspectorChartThemeService: InspectorChartThemeService,
    ) {}

    getData(range: number[]): Observable<IInspectorChartData> {
        return this.inspectorChartDataService.getData(this.apiUrl, range);
    }

    makeChartData({charts}: IInspectorChartData): PrimitiveArray[] {
        this.minAgentIdList = makeYData(charts.y['TOTAL_THREAD_COUNT'], 1) as string[];
        this.maxAgentIdList = makeYData(charts.y['TOTAL_THREAD_COUNT'], 3) as string[];

        return [
            ['x', ...makeXData(charts.x)],
            ['max', ...makeYData(charts.y['TOTAL_THREAD_COUNT'], 2)],
            ['avg', ...makeYData(charts.y['TOTAL_THREAD_COUNT'], 4)],
            ['min', ...makeYData(charts.y['TOTAL_THREAD_COUNT'], 0)],
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
                    text: 'Total Thread (count)',
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
        return value.toString();
    }

    getTooltipFormat(v: number, columnId: string, i: number): string {
        return `${this.convertWithUnit(v)} ${getAgentId(columnId, i, this.minAgentIdList, this.maxAgentIdList)}`;
    }
}
