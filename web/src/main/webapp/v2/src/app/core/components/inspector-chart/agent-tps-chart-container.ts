import { PrimitiveArray, Data } from 'billboard.js';
import { Observable } from 'rxjs';

import { IInspectorChartContainer } from './inspector-chart-container-factory';
import { makeYData, makeXData, getMaxTickValue, getStackedData } from 'app/core/utils/chart-util';
import { IInspectorChartData, InspectorChartDataService } from './inspector-chart-data.service';

export class AgentTPSChartContainer implements IInspectorChartContainer {
    private apiUrl = 'getAgentStat/transaction/chart.pinpoint';

    defaultYMax = 4;
    title = 'Transactions Per Second';

    constructor(
        private inspectorChartDataService: InspectorChartDataService
    ) {}

    getData(range: number[]): Observable<IInspectorChartData | AjaxException> {
        return this.inspectorChartDataService.getData(this.apiUrl, range);
    }

    makeChartData({charts}: IInspectorChartData): PrimitiveArray[] {
        return [
            ['x', ...makeXData(charts.x)],
            ['tpsSC', ...makeYData(charts.y['TPS_SAMPLED_CONTINUATION'], 2)],
            ['tpsSN', ...makeYData(charts.y['TPS_SAMPLED_NEW'], 2)],
            ['tpsUC', ...makeYData(charts.y['TPS_UNSAMPLED_CONTINUATION'], 2)],
            ['tpsUN', ...makeYData(charts.y['TPS_UNSAMPLED_NEW'], 2)],
            ['tpsSSN', ...makeYData(charts.y['TPS_SKIPPED_NEW'], 2)],
            ['tpsSSC', ...makeYData(charts.y['TPS_SKIPPED_CONTINUATION'], 2)],
            ['tpsT', ...makeYData(charts.y['TPS_TOTAL'], 2)],
        ];
    }

    makeDataOption(): Data {
        return {
            type: 'area-spline',
            names: {
                tpsSC: 'S.C',
                tpsSN: 'S.N',
                tpsUC: 'U.C',
                tpsUN: 'U.N',
                tpsSSN: 'S.S.N',
                tpsSSC: 'S.S.C',
                tpsT: 'Total'
            },
            colors: {
                tpsSC: 'rgba(214, 141, 8, 0.4)',
                tpsSN: 'rgba(252, 178, 65, 0.4)',
                tpsUC: 'rgba(90, 103, 166, 0.4)',
                tpsUN: 'rgba(160, 153, 255, 0.4)',
                tpsSSN: 'rgba(26, 188, 156, 0.4)',
                tpsSSC: 'rgba(82, 190, 128, 0.4)',
                tpsT: 'rgb(255, 255, 255)'
            },
            groups: [
                ['tpsSC', 'tpsSN', 'tpsUC', 'tpsUN', 'tpsSSN', 'tpsSSC', 'tpsT']
            ],
            order: null
        };
    }

    makeElseOption(): {[key: string]: any} {
        return {};
    }

    makeYAxisOptions(data: PrimitiveArray[]): {[key: string]: any} {
        return {
            y: {
                label: {
                    text: 'Transaction (count)',
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
                    const maxTickValue = getMaxTickValue(getStackedData(data.slice(0, -1)), 1);

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
                : (arr.splice(i + 1), `${v}${curr}`);
        }, value.toString());
    }

    getTooltipFormat(v: number, columnId: string, i: number): string {
        return this.convertWithUnit(v);
    }
}
