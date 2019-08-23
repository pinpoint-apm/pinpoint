import { PrimitiveArray, Data } from 'billboard.js';
import { Observable } from 'rxjs';

import { IInspectorChartContainer } from './inspector-chart-container-factory';
import { makeYData, makeXData, getMaxTickValue } from './inspector-chart-util';
import { IInspectorChartData, InspectorChartDataService } from './inspector-chart-data.service';

export class ApplicationOpenFileDescriptorChartContainer implements IInspectorChartContainer {
    private apiUrl = 'getApplicationStat/fileDescriptor/chart.pinpoint';
    defaultYMax = 100;
    title = 'Open File Descriptor';

    constructor(
        private inspectorChartDataService: InspectorChartDataService
    ) {}

    getData(range: number[]): Observable<IInspectorChartData | AjaxException> {
        return this.inspectorChartDataService.getData(this.apiUrl, range);
    }

    makeChartData({charts}: IInspectorChartData): PrimitiveArray[] {
        return [
            ['x', ...makeXData(charts.x)],
            ['min', ...makeYData(charts.y['OPEN_FILE_DESCRIPTOR_COUNT'], 0)],
            ['avg', ...makeYData(charts.y['OPEN_FILE_DESCRIPTOR_COUNT'], 4)],
            ['max', ...makeYData(charts.y['OPEN_FILE_DESCRIPTOR_COUNT'], 2)],
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
                    text: 'File Descriptor (count)',
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
        return makeYData(charts.y['OPEN_FILE_DESCRIPTOR_COUNT'], 1) as string[];
    }

    makeMaxAgentIdList({charts}: IInspectorChartData): string[] {
        return makeYData(charts.y['OPEN_FILE_DESCRIPTOR_COUNT'], 3) as string[];
    }

    convertWithUnit(value: number): string {
        return value.toString();
    }
}
