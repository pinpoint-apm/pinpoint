import { PrimitiveArray, Data } from 'billboard.js';
import { Observable } from 'rxjs';

import { IInspectorChartContainer } from './inspector-chart-container-factory';
import { makeYData, makeXData, getMaxTickValue } from 'app/core/utils/chart-util';
import { IInspectorChartData, InspectorChartDataService } from './inspector-chart-data.service';

export class AgentJVMNonHeapChartContainer implements IInspectorChartContainer {
    private apiUrl = 'getAgentStat/jvmGc/chart.pinpoint';
    private fgcCount: number[];

    defaultYMax = 100;
    title = 'Non Heap Usage';

    constructor(
        private inspectorChartDataService: InspectorChartDataService
    ) {}

    getData(range: number[]): Observable<IInspectorChartData | AjaxException> {
        return this.inspectorChartDataService.getData(this.apiUrl, range);
    }

    makeChartData({charts}: IInspectorChartData): PrimitiveArray[] {
        const gcOldTime = makeYData(charts.y['JVM_GC_OLD_TIME'], 3) as number[];
        const gcOldCount = this.fgcCount = makeYData(charts.y['JVM_GC_OLD_COUNT'], 3) as number[];
        let totalSumGCTime = 0;
        const fgcTime = gcOldTime.map((t: number, i: number) => {
            if (i >= 1 && gcOldCount[i - 1] > 0) {
                totalSumGCTime = 0;
            }

            if (t > 0) {
                totalSumGCTime += t;
            }

            return gcOldCount[i] > 0 ? totalSumGCTime : null;
        });

        return [
            ['x', ...makeXData(charts.x)],
            ['max', ...makeYData(charts.y['JVM_MEMORY_NON_HEAP_MAX'], 1)],
            ['used', ...makeYData(charts.y['JVM_MEMORY_NON_HEAP_USED'], 1)],
            ['fgcTime', ...fgcTime],
        ];
    }

    makeDataOption(): Data {
        return {
            types: {
                max: 'spline',
                used: 'area-spline',
                fgcTime: 'bar'
            },
            names: {
                max: 'Max',
                used: 'Used',
                fgcTime: 'Major GC'
            },
            colors: {
                max: 'rgba(174, 199, 232, 0.4)',
                used: 'rgba(31, 119, 180, 0.4)',
                fgcTime: 'rgba(255, 42, 0, 0.3)'
            },
            axes: {
                max: 'y',
                used: 'y',
                fgcTime: 'y2'
            },
        };
    }

    makeElseOption(): {[key: string]: any} {
        return {
            padding: {
                top: 20,
                bottom: 15,
                right: 65,
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
                    const maxTickValue = getMaxTickValue(data, 1, 3);

                    return maxTickValue === 0 ? this.defaultYMax : maxTickValue;
                })(),
                default: [0, this.defaultYMax]
            },
            y2: {
                show: true,
                label: {
                    text: 'Full GC (ms)',
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
                    const maxTickValue = getMaxTickValue(data, 3);

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
        return columnId === 'fgcTime' ? `${v}ms ${this.fgcCount[i] > 1 ? '(' + this.fgcCount[i] + ')' : ''}` : this.convertWithUnit(v);
    }
}
