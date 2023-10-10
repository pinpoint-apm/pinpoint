import { Component, OnInit, ViewChild, ElementRef, Input, Output, EventEmitter, HostBinding, OnDestroy } from '@angular/core';
import bb, { Chart, bar, zoom } from 'billboard.js';
import * as moment from 'moment-timezone';
import { Subject } from 'rxjs';

import { StoreHelperService } from 'app/shared/services';
import { ChartConfig } from './url-statistic-chart-container.component';

@Component({
    selector: 'pp-url-statistic-bar-chart',
    templateUrl: './url-statistic-chart.component.html',
    styleUrls: ['./url-statistic-chart.component.css']
})
export class UrlStatisticBarChartComponent implements OnInit, OnDestroy {
    @HostBinding('class') hostClass = 'l-url-statistic-chart';
    @ViewChild('chartHolder', { static: true }) chartHolder: ElementRef;
    @Input() chartOptions: {type: string} & ChartConfig;
    @Input() chartData: {x: number[], y: {[key: string]: (number | null)[]}};
    @Input() emptyMessage: string;
    @Output() outRendered = new EventEmitter<void>(true);

    private chartInstance: Chart;
    private unsubscribe = new Subject<void>();
    private timezone: string;
    private dateFormatMonth: string;
    private dateFormatDay: string;
    private defaultYMax = 1;

    constructor(
        private storeHelperService: StoreHelperService,
    ) {
        this.initTimeDateFormat();
    }

    ngOnInit() {
        const {x, y} = this.chartData;
        const keyList = Object.keys(y);
        const {colors} = this.chartOptions;

        this.chartInstance = bb.generate({
            bindto: this.chartHolder.nativeElement,
            data: {
                x: 'x',
                columns: [
                    ['x', ...x],
                    ...Object.entries(y).map(([key, value]: [string, number[]]) => [key, ...value]),
                ],
                empty: {
                    label: {
                        text: this.emptyMessage
                    }
                },
                type: bar(),
                colors: keyList.reduce((acc: {[key: string]: string}, curr: string, i: number) => {
                    return {...acc, [curr]: colors[i]};
                }, {}),
                groups: [keyList],
                order: null
            },
            ...this.getChartOption(this.chartOptions),
            onrendered: () => this.finishLoading(),
        });
    }

    ngOnDestroy(): void {
        this.unsubscribe.next();
		this.unsubscribe.complete();   
    }

    private initTimeDateFormat(): void {
        this.storeHelperService.getTimezone(this.unsubscribe).subscribe((timezone: string) => {
            this.timezone = timezone;
        });

        this.storeHelperService.getDateFormatArray(this.unsubscribe, 6, 7).subscribe(([dateFormatMonth, dateFormatDay]: string[]) => {
            this.dateFormatMonth = dateFormatMonth;
            this.dateFormatDay = dateFormatDay;
        });  
    }

    updateChart({x, y}: {x: number[], y: {[key: string]: (number | null)[]}}): void {
        this.chartInstance.load({
            columns: [
                ['x', ...x],
                ...Object.entries(y).map(([key, value]: [string, number[]]) => [key, ...value]),
            ],
        });
    }

    private finishLoading(): void {
        this.outRendered.emit();
    }

    private getChartOption({valueFormat, yAxis}: ChartConfig): {[key: string]: any} {
        return {
            bar: {
                width: {
                    ratio: 0.8
                }
            },
            padding: {
                top: 20,
                bottom: 20,
                right: 20
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
                    label: {
                        text: yAxis.label,
                        position: 'outer-middle'
                    },
                    tick: {
                        // count: 2,
                        format: (v: number): string => valueFormat(v),
                    },
                    padding: {
                        // top: 0,
                        bottom: 0
                    },
                    min: 0,
                    // max: yMax,
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
                    value: (v: number): string => valueFormat(v)
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
}
