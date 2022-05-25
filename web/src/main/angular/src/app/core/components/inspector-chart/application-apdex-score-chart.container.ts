import { PrimitiveArray, Data, spline } from 'billboard.js';
import { Observable } from 'rxjs';

import { IInspectorChartContainer } from './inspector-chart-container-factory';
import { makeYData, makeXData } from 'app/core/utils/chart-util';
import { IInspectorChartData, InspectorChartDataService } from './inspector-chart-data.service';
import { getAgentId } from './inspector-chart-util';
import { InspectorChartThemeService } from './inspector-chart-theme.service';
import { NewUrlStateNotificationService } from 'app/shared/services';
import { UrlPathId } from 'app/shared/models';

export class ApplicationApdexScoreChartContainer implements IInspectorChartContainer {
    private apiUrl = 'getApplicationStat/apdexScore/chart.pinpoint';
    private minAgentIdList: string[];
    private maxAgentIdList: string[];

    defaultYMax = 1;
    title = 'Apdex Score';

    constructor(
        private inspectorChartDataService: InspectorChartDataService,
        private inspectorChartThemeService: InspectorChartThemeService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
    ) {}

    getData(range: number[]): Observable<IInspectorChartData> {
        const applicationId = this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getApplicationName();
        const serviceTypeName = this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getServiceType();

        return this.inspectorChartDataService.getData(this.apiUrl, range, {serviceTypeName, applicationId});
    }

    makeChartData({charts}: IInspectorChartData): PrimitiveArray[] {
        this.minAgentIdList = makeYData(charts.y['APDEX_SCORE'], 1) as string[];
        this.maxAgentIdList = makeYData(charts.y['APDEX_SCORE'], 3) as string[];

        return [
            ['x', ...makeXData(charts.x)],
            ['max', ...makeYData(charts.y['APDEX_SCORE'], 2)],
            ['avg', ...makeYData(charts.y['APDEX_SCORE'], 4)],
            ['min', ...makeYData(charts.y['APDEX_SCORE'], 0)],
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
                    text: 'Apdex Score',
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
        return {
            linked: false,
        };
    }

    convertWithUnit(value: number): string {
        return (Math.floor(value * 100) / 100).toFixed(2);
    }

    getTooltipFormat(v: number, columnId: string, i: number): string {
        return `${this.convertWithUnit(v)} ${getAgentId(columnId, i, this.minAgentIdList, this.maxAgentIdList)}`;
    }
}
