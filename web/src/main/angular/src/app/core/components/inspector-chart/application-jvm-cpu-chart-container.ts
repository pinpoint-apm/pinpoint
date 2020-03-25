import { PrimitiveArray, Data } from 'billboard.js';
import { Observable } from 'rxjs';

import { IInspectorChartContainer } from './inspector-chart-container-factory';
import { makeYData, makeXData } from 'app/core/utils/chart-util';
import { IInspectorChartData, InspectorChartDataService } from './inspector-chart-data.service';
import { getAgentId } from './inspector-chart-util';

export class ApplicationJVMCpuChartContainer implements IInspectorChartContainer {
    private apiUrl = 'getApplicationStat/cpuLoad/chart.pinpoint';
    private minAgentIdList: string[];
    private maxAgentIdList: string[];

    defaultYMax = 100;
    title = 'JVM CPU Usage';

    constructor(
        private inspectorChartDataService: InspectorChartDataService
    ) {}

    getData(range: number[]): Observable<IInspectorChartData | AjaxException> {
        return this.inspectorChartDataService.getData(this.apiUrl, range);
    }

    makeChartData({charts}: IInspectorChartData): PrimitiveArray[] {
        this.minAgentIdList = makeYData(charts.y['CPU_LOAD_JVM'], 1) as string[];
        this.maxAgentIdList = makeYData(charts.y['CPU_LOAD_JVM'], 3) as string[];

        return [
            ['x', ...makeXData(charts.x)],
            ['max', ...makeYData(charts.y['CPU_LOAD_JVM'], 2)],
            ['avg', ...makeYData(charts.y['CPU_LOAD_JVM'], 4)],
            ['min', ...makeYData(charts.y['CPU_LOAD_JVM'], 0)],
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
                    text: 'CPU Usage (%)',
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
                max: this.defaultYMax,
                default: [0, this.defaultYMax]
            }
        };
    }

    convertWithUnit(value: number): string {
        return `${value}%`;
    }

    getTooltipFormat(v: number, columnId: string, i: number): string {
        return `${this.convertWithUnit(v)} ${getAgentId(columnId, i, this.minAgentIdList, this.maxAgentIdList)}`;
    }
}
