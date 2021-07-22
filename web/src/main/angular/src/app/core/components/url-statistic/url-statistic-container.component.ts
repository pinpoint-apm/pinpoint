import { map, tap } from 'rxjs/operators';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { Data, PrimitiveArray, bar, zoom } from 'billboard.js';
import * as moment from 'moment-timezone';
import { Subject } from 'rxjs';

import { UrlStatisticDataService } from './url-statistic-data.service';
import { makeYData, makeXData, getMaxTickValue, getStackedData } from 'app/core/utils/chart-util';
import { StoreHelperService, WebAppSettingDataService } from 'app/shared/services';

@Component({
    selector: 'pp-url-statistic-container',
    templateUrl: './url-statistic-container.component.html',
    styleUrls: ['./url-statistic-container.component.css']
})
export class UrlStatisticContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private defaultYMax = 100;
    private chartColors: string[];
    private timezone: string;
    private dateFormatMonth: string;
    private dateFormatDay: string;

    urlStatisticData: any[];
    chartConfig: IChartConfig;
    showLoading = true;

    constructor(
        private urlStatisticDataService: UrlStatisticDataService,
        private webAppSettingDataService: WebAppSettingDataService,
        private storeHelperService: StoreHelperService,
    ) { }

    ngOnInit() {
        this.initChartColors();
        this.listenToEmitter();
        this.initData();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    onRendered(): void {
        this.showLoading = false;
    }

    onSelectUrl(selectedUrl: string): void {
        // TODO: Refactor/extract set chartdata logic
        const data = this.urlStatisticData.find(({uri}: any) => uri === selectedUrl);

        this.setChartConfig(this.makeChartData(data));
    }

    private initChartColors(): void {
        this.chartColors = this.webAppSettingDataService.getColorByRequestInDetail();
    }

    private listenToEmitter(): void {
        this.storeHelperService.getTimezone(this.unsubscribe).subscribe((timezone: string) => {
            this.timezone = timezone;
        });

        this.storeHelperService.getDateFormatArray(this.unsubscribe, 6, 7).subscribe(([dateFormatMonth, dateFormatDay]: string[]) => {
            this.dateFormatMonth = dateFormatMonth;
            this.dateFormatDay = dateFormatDay;
        });
    }

    private initData(): void {
        // TODO: Add Error handling
        this.urlStatisticDataService.getData().pipe(
            tap((data: any[]) => {
                console.log(data);
                this.urlStatisticData = data;
            }),
            map((data: any[]) => this.makeChartData(data[0]))
        ).subscribe((data: PrimitiveArray[]) => {
            this.setChartConfig(data);
        });
    }

    private makeChartData({charts}: any): PrimitiveArray[] {
        // const data = charts.y['HISTOGRAM_BUCKET'];
        // const parsedData = data.reduce((acc: number[][], curr: number[]) => {
        //     return acc.length === 0 ? curr.map((c: number) => [c])
        //         : acc.map((a: number[], i: number) => [...a, curr[i]]);
        // }, []);

        // * Use mock data temporarily
        const size = 128;
        const startDatetime = 1607463120000;
        const interval = 1000 * 60;

        const x = Array(size)
            .fill(0)
            .map((v, i) => startDatetime + interval * i);

        const data = [300, 200, 100, 50, 30, 20, 10, 10]
            .map(v => Array(size)
                .fill(0)
                .map(() => Math.round(Math.random() * v))
            );

        return [
            ['x', ...x],
            ['0 ~ 100', ...data[0]],
            ['100 ~ 300', ...data[1]],
            ['300 ~ 500', ...data[2]],
            ['500 ~ 1000', ...data[3]],
            ['1000 ~ 3000', ...data[4]],
            ['3000 ~ 5000', ...data[5]],
            ['5000 ~ 8000', ...data[6]],
            ['8000 ~ ', ...data[7]],
            // ['x', ...makeXData(charts.x)],
            // ['0 ~ 100', ...makeYData(charts.y['HISTOGRAM_BUCKET'], 0)],
            // ['100 ~ 300', ...makeYData(charts.y['HISTOGRAM_BUCKET'], 1)],
            // ['300 ~ 500', ...makeYData(charts.y['HISTOGRAM_BUCKET'], 2)],
            // ['500 ~ 1000', ...makeYData(charts.y['HISTOGRAM_BUCKET'], 3)],
            // ['1000 ~ 3000', ...makeYData(charts.y['HISTOGRAM_BUCKET'], 4)],
            // ['3000 ~ 5000', ...makeYData(charts.y['HISTOGRAM_BUCKET'], 5)],
            // ['5000 ~ 8000', ...makeYData(charts.y['HISTOGRAM_BUCKET'], 6)],
            // ['8000 ~ ', ...makeYData(charts.y['HISTOGRAM_BUCKET'], 7)],
        ];
    }

    private setChartConfig(data: PrimitiveArray[]): void {
        this.chartConfig =  {
            dataConfig: this.makeDataOption(data),
            elseConfig: this.makeElseOption(getMaxTickValue(getStackedData(data), 1)),
        };
    }

    private makeDataOption(columns: PrimitiveArray[]): Data {
        const keyList = columns.slice(1).map(([key]: PrimitiveArray) => key as string);

        return {
            x: 'x',
            columns,
            // empty: {
            //     label: {
            //         text: this.dataEmptyText
            //     }
            // },
            type: bar(),
            colors: keyList.reduce((acc: {[key: string]: string}, curr: string, i: number) => {
                return { ...acc, [curr]: this.chartColors[i] };
            }, {}),
            groups: [keyList],
            order: null
        };
    }

    private makeElseOption(yMax: number): {[key: string]: any} {
        return {
            bar: {
                width: {
                    ratio: 0.8
                }
            },
            padding: {
                top: 20,
                bottom: 20,
                right: 10
            },
            axis: {
                x: {
                    type: 'timeseries',
                    tick: {
                        count: 6,
                        show: false,
                        format: (time: Date) => {
                            return moment(time).tz(this.timezone).format(this.dateFormatMonth) + '\n' + moment(time).tz(this.timezone).format(this.dateFormatDay);
                        }
                    },
                    padding: {
                        left: 0,
                        right: 0
                    }
                },
                y: {
                    tick: {
                        count: 3,
                        format: (v: number): string => this.convertWithUnit(v)
                    },
                    padding: {
                        top: 0,
                        bottom: 0
                    },
                    min: 0,
                    max: yMax,
                    default: [0, this.defaultYMax]
                }
            },
            grid: {
                y: {
                    show: true
                }
            },
            tooltip: {
                order: '',
                format: {
                    value: (v: number) => this.addComma(v.toString())
                }
            },
            transition: {
                duration: 0
            },
            zoom: {
                enabled: zoom()
            },
        };
    }

    private addComma(str: string): string {
        return str.replace(/(\d)(?=(?:\d{3})+(?!\d))/g, '$1,');
    }

    private convertWithUnit(value: number): string {
        const unitList = ['', 'K', 'M', 'G'];

        return [...unitList].reduce((acc: string, curr: string, i: number, arr: string[]) => {
            const v = Number(acc);

            return v >= 1000
                ? (v / 1000).toString()
                : (arr.splice(i + 1), Number.isInteger(v) ? `${v}${curr}` : `${v.toFixed(2)}${curr}`);
        }, value.toString());
    }
}
