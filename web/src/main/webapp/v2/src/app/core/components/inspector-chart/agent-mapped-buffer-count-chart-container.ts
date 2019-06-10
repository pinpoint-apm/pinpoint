import { PrimitiveArray, Data } from 'billboard.js';
import { Observable } from 'rxjs';

import { IInspectorChartContainer } from './inspector-chart-container-factory';
import { makeYData, makeXData, getMaxTickValue } from './inspector-chart-util';
import { IInspectorChartData, InspectorChartDataService } from './inspector-chart-data.service';

export class AgentMappedBufferCountChartContainer implements IInspectorChartContainer {
    private apiUrl = 'getAgentStat/directBuffer/chart.pinpoint';
    defaultYMax = 100;
    title = 'Mapped Buffer Count';

    constructor(
        private inspectorChartDataService: InspectorChartDataService
    ) {}

    getData(range: number[]): Observable<IInspectorChartData | AjaxException> {
        return this.inspectorChartDataService.getData(this.apiUrl, range);
    }

    makeChartData({charts}: IInspectorChartData): PrimitiveArray[] {
        return [
            ['x', ...makeXData(charts.x)],
            ['mappedCount', ...makeYData(charts.y['MAPPED_COUNT'], 2)],
        ];
    }

    makeDataOption(): Data {
        return {
            type: 'area-spline',
            names: {
                mappedCount: 'Mapped Buffer Count',
            },
            colors: {
                mappedCount: 'rgba(31, 119, 180, 0.4)',
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
                    const max = Math.max(...data.slice(1).map((d: PrimitiveArray) => d.slice(1)).flat() as number[]);
                    const quarter = max / 4;

                    return max === 0 ? getMaxTickValue(this.defaultYMax) : getMaxTickValue(max + quarter);
                })(),
                default: [0, getMaxTickValue(this.defaultYMax)]
            }
        };
    }

    convertWithUnit(value: number): string {
        return value.toString();
    }
}
