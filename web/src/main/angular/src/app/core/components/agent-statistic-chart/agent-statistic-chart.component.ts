import { Component, OnInit, Input, ViewChild, ElementRef, OnChanges, SimpleChanges, HostBinding } from '@angular/core';
import bb, { PrimitiveArray, Data } from 'billboard.js';

import { getMaxTickValue } from 'app/core/utils/chart-util';

@Component({
    selector: 'pp-agent-statistic-chart',
    templateUrl: './agent-statistic-chart.component.html',
    styleUrls: ['./agent-statistic-chart.component.css']
})
export class AgentStatisticChartComponent implements OnInit, OnChanges {
    @HostBinding('class') hostClass = 'l-agent-statistic-chart';
    @ViewChild('chartHolder', { static: true }) chartHolder: ElementRef;
    @Input() title: string;
    @Input() barColor: string;
    @Input() emptyText: string;
    @Input() chartData: PrimitiveArray[];

    private chartInstance: any;
    private defaultYMax = 10;

    constructor() {}
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        Object.keys(changes)
            .filter((propName: string) => {
                return changes[propName].currentValue;
            })
            .forEach((propName: string) => {
                const changedProp = changes[propName];

                switch (propName) {
                    case 'chartData':
                        this.chartInstance ? this.updateChart(changedProp) : this.initChart();
                        break;
                }
            });
    }

    private initChart(): void {
        this.chartInstance = bb.generate({
            bindto: this.chartHolder.nativeElement,
            data: this.makeDataOption(this.chartData),
            ...this.makeElseOption(this.chartData),
        });
    }

    private updateChart({previousValue, currentValue}: {previousValue: PrimitiveArray[], currentValue: PrimitiveArray[]}): void {
        const prevColumns = previousValue;
        const currColumns = currentValue;

        const prevKeys = prevColumns.map(([key]: PrimitiveArray) => key);
        const currKeys = currColumns.map(([key]: PrimitiveArray) => key);
        const removedKeys = prevKeys.filter((key: string) => !currKeys.includes(key));
        const unload = prevKeys.length === 0 ? false
            : removedKeys.length !== 0 ? true
            : this.getEmptyDataKeys(currColumns);
        const yMax = this.getYMax(currColumns);

        this.chartInstance.config('axis.y.max', yMax);
        this.chartInstance.load({
            columns: currColumns,
            unload,
        });
    }

    private getEmptyDataKeys(data: PrimitiveArray[]): string[] {
        return data.slice(1).filter((d: PrimitiveArray) => d.length === 1).map(([key]: PrimitiveArray) => key as string);
    }

    private makeDataOption(columns: PrimitiveArray[]): Data {
        const key = columns[1][0] as string;

        return {
            x: 'x',
            columns,
            names: {
                [key]: this.title
            },
            empty: {
                label: {
                    text: this.emptyText
                }
            },
            type: 'bar',
            labels: true,
            color: () => this.barColor
        };
    }

    private makeElseOption(data: PrimitiveArray[]): {[key: string]: any} {
        return {
            padding: {
                top: 20
            },
            axis: {
                x: {
                    type: 'category'
                },
                y: {
                    // tick: {
                    //     count: 3,
                    //     format: (v: number): string => this.convertWithUnit(v)
                    // },
                    padding: {
                        top: 0,
                        bottom: 0
                    },
                    min: 0,
                    max: this.getYMax(data),
                    default: [0, this.defaultYMax]
                },
                rotated: true
            },
            grid: {
                y: {
                    show: true
                }
            },
            tooltip: {
                show: false
            },
            transition: {
                duration: 0
            },
            zoom: {
                enabled: true
            },
        };
    }

    private getYMax(data: PrimitiveArray[]): number {
        const maxTickValue = getMaxTickValue(data, 1);
        const yMax = maxTickValue === 0 ? this.defaultYMax : maxTickValue;

        return yMax;
    }
}
