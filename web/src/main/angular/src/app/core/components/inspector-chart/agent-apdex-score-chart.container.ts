import { PrimitiveArray, Data, spline } from 'billboard.js';
import { Observable } from 'rxjs';

import { IInspectorChartContainer } from './inspector-chart-container-factory';
import { makeYData, makeXData, getMaxTickValue } from 'app/core/utils/chart-util';
import { IInspectorChartData, InspectorChartDataService } from './inspector-chart-data.service';
import { InspectorChartThemeService } from './inspector-chart-theme.service';
import { NewUrlStateNotificationService } from 'app/shared/services';
import { UrlPathId } from 'app/shared/models';

export class AgentApdexScoreChartContainer implements IInspectorChartContainer {
    private apiUrl = 'getAgentStat/apdexScore/chart.pinpoint';

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
        return [
            ['x', ...makeXData(charts.x)],
            ['apdexScore', ...makeYData(charts.y['APDEX_SCORE'], 2)],
        ];
    }

    makeDataOption(): Data {
        const alpha = this.inspectorChartThemeService.getAlpha(0.4);

        return {
            type: spline(),
            names: {
                apdexScore: 'Apdex Score',
            },
            colors: {
                apdexScore: `rgba(65, 196, 100, ${alpha})`,
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
            linked: false
        };
    }

    convertWithUnit(value: number): string {
        return (Math.floor(value * 100) / 100).toFixed(2);
    }

    getTooltipFormat(v: number, columnId: string, i: number): string {
        return this.convertWithUnit(v);
    }
}
