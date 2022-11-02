import { Component, ElementRef, OnDestroy, OnInit } from '@angular/core';
import { iif, of, Subject } from 'rxjs';
import { map, switchMap, takeUntil, tap } from 'rxjs/operators';
import { Data, PrimitiveArray, bar, zoom, areaStep } from 'billboard.js';
import * as moment from 'moment-timezone';

import { MessageQueueService, MESSAGE_TO, NewUrlStateNotificationService, StoreHelperService, WebAppSettingDataService } from 'app/shared/services';
import { UrlStatisticChartDataService } from './url-statistic-chart-data.service';
import { UrlPathId } from 'app/shared/models';
import { makeYData, makeXData, getMaxTickValue, getStackedData } from 'app/core/utils/chart-util';

@Component({
    selector: 'pp-url-statistic-chart-container',
    templateUrl: './url-statistic-chart-container.component.html',
	styleUrls: ['./url-statistic-chart-container.component.css']
})
export class UrlStatisticChartContainerComponent implements OnInit, OnDestroy {
	private unsubscribe = new Subject<void>();
	private defaultYMax = 100;
    private chartColorList: string[];
    private timezone: string;
    private dateFormatMonth: string;
    private dateFormatDay: string;
	private cachedData: {[key: string]: {timestamp: number[], metricValues: IMetricValue[]}} = {};
    private fieldNameList: string[];
    
    isUriSelected: boolean;
	chartConfig: IChartConfig;
    showLoading = true;

	constructor(
		private messageQueueService: MessageQueueService,
		private webAppSettingDataService: WebAppSettingDataService,
        private storeHelperService: StoreHelperService,
		private newUrlStateNotificationService: NewUrlStateNotificationService,
		private urlStatisticChartDataService: UrlStatisticChartDataService,
        private el: ElementRef,
	) { }

	ngOnInit() {
        this.initFieldNameList();
		this.initChartColorList();
        this.listenToEmitter();

		this.newUrlStateNotificationService.onUrlStateChange$.pipe(
			takeUntil(this.unsubscribe)
		).subscribe(() => {
			this.cachedData = {};
            this.isUriSelected = false;
            this.chartConfig = null;
		});

		this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SELECT_URL_INFO).pipe(
            tap(() => this.isUriSelected = true),
			switchMap((uri: string) => {
				if (Boolean(this.cachedData[uri])) {
					return of(this.cachedData[uri]);
				} else {
					const urlService = this.newUrlStateNotificationService;
					const from = urlService.getStartTimeToNumber();
					const to = urlService.getEndTimeToNumber();
					const applicationName = urlService.getPathValue(UrlPathId.APPLICATION).getApplicationName();
					const agentId = urlService.getPathValue(UrlPathId.AGENT_ID) || '';
					const params = {from, to, applicationName, agentId, uri};

					// TODO: Add error handling?
					return this.urlStatisticChartDataService.getData(params).pipe(
						map(({timestamp, metricValueGroups}: IUrlStatChartData) => {
							this.cachedData[uri] = {timestamp, metricValues: metricValueGroups[0].metricValues};
							return this.cachedData[uri];
						}),
					);	
				}
			})
		).subscribe((data: {timestamp: number[], metricValues: IMetricValue[]}) => {
			this.setChartConfig(this.makeChartData(data));
		});
	}

	ngOnDestroy(): void {
		this.unsubscribe.next();
		this.unsubscribe.complete();
	}

	onRendered(): void {

	}

    private initFieldNameList(): void {
        this.fieldNameList = this.webAppSettingDataService.getUrlStatFieldNameList();
    }

	private initChartColorList(): void {
        const computedStyle = getComputedStyle(this.el.nativeElement);

        this.chartColorList = [
            computedStyle.getPropertyValue('--chart-most-success'),
            computedStyle.getPropertyValue('--chart-success'),
            computedStyle.getPropertyValue('--chart-kinda-success'),
            computedStyle.getPropertyValue('--chart-almost-normal'),
            computedStyle.getPropertyValue('--chart-normal'),
            computedStyle.getPropertyValue('--chart-slow'),
            computedStyle.getPropertyValue('--chart-very-slow'),
            computedStyle.getPropertyValue('--chart-fail'),
        ]
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

	private makeChartData({timestamp, metricValues}: {[key: string]: any}): PrimitiveArray[] {
		return [
            ['x', ...makeXData(timestamp)],
            ...metricValues.map(({values}: IMetricValue, i: number) => {
                return [this.fieldNameList[i], ...values.map((v: number) => v < 0 ? null : v)];
            })
        ];
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
            // type: areaStep(),
            colors: keyList.reduce((acc: {[key: string]: string}, curr: string, i: number) => {
                return { ...acc, [curr]: this.chartColorList[i] };
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
            point: {
                show: false,
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
