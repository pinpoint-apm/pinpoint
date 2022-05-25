import { PrimitiveArray, Data, areaSpline, spline } from 'billboard.js';
import { Observable } from 'rxjs';

import { IInspectorChartContainer } from './inspector-chart-container-factory';
import { makeYData, makeXData } from 'app/core/utils/chart-util';
import { IInspectorChartData, InspectorChartDataService } from './inspector-chart-data.service';
import { InspectorChartThemeService } from './inspector-chart-theme.service';

export class AgentCPUChartContainer implements IInspectorChartContainer {
    private apiUrl = 'getAgentStat/cpuLoad/chart.pinpoint';

    defaultYMax = 100;
    title = 'JVM/System CPU Usage';

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
            ['jvm', ...makeYData(charts.y['CPU_LOAD_JVM'], 1)],
            ['system', ...makeYData(charts.y['CPU_LOAD_SYSTEM'], 1)],
        ];
    }

    makeDataOption(): Data {
        const alpha = this.inspectorChartThemeService.getAlpha(0.4);

        return {
            types: {
                jvm: areaSpline(),
                system: spline()
            },
            names: {
                jvm: 'JVM',
                system: 'System'
            },
            colors: {
                jvm: `rgba(174, 199, 232, ${alpha})`,
                system: `rgba(31, 119, 180, ${alpha})`
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

    makeTooltipOptions(): {[key: string]: any} {
        return {};
    }

    convertWithUnit(value: number): string {
        return `${value}%`;
    }

    getTooltipFormat(v: number, columnId: string, i: number): string {
        return this.convertWithUnit(v);
    }
}
