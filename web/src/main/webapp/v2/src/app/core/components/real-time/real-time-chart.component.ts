import { Component, OnInit, ViewChild, ElementRef, Input, OnChanges, SimpleChanges, Renderer2 } from '@angular/core';
import { Chart, ChartPoint, ChartDataSets } from 'chart.js';
import 'chartjs-plugin-streaming';
import * as moment from 'moment-timezone';

export interface IRealTimeChartData {
    timeStamp: number;
    responseCount: number[];
}

@Component({
    selector: 'pp-real-time-chart',
    templateUrl: './real-time-chart.component.html',
    styleUrls: ['./real-time-chart.component.css']
})
export class RealTimeChartComponent implements OnInit, OnChanges {
    @ViewChild('real') chartElement: ElementRef;
    @ViewChild('tooltip') tooltip: ElementRef;
    @ViewChild('tooltipCaret') tooltipCaret: ElementRef;
    @Input() showAxis: boolean;
    @Input() namespace: string;
    @Input() timezone: string;
    @Input() dateFormat: string;
    @Input() chartData: IRealTimeChartData;

    private chartObj: Chart;
    private defaultYMax = 5;

    chartColors = ['#33b692', '#51afdf', '#fea63e', '#e76f4b'];
    chartLabels = ['1s', '3s', '5s', 'Slow'];
    showTooltip: boolean;
    tooltipDataObj = {
        title: '',
        values: [] as number[],
    };
    // private yMaxHolder: number[] = [];

    constructor(
        private renderer: Renderer2,
        private el: ElementRef
    ) {}
    ngOnChanges(changes: SimpleChanges) {
        Object.keys(changes)
            .filter(() => this.chartObj !== undefined)
            .filter((propName: string) => {
                return changes[propName].currentValue;
            })
            .forEach((propName: string) => {
                const changedProp = changes[propName];

                switch (propName) {
                    case 'chartData':
                        this.updateChartData(changedProp.currentValue);
                        break;
                }

                this.chartObj.update({
                    duration: 0
                });
            });
    }
    ngOnInit() {
        this.chartObj = new Chart(this.chartElement.nativeElement.getContext('2d'), {
            type: 'line',
            data: {
                labels: [],
                datasets: this.chartColors.map((color: string, i: number) => {
                    return {
                        label: this.chartLabels[i],
                        backgroundColor: color,
                        borderColor: color,
                        borderWidth: 0.5,
                        fill: true,
                        pointRadius: 0,
                        pointHoverRadius: 3,
                        data: []
                    };
                })
            },
            options: {
                animation: {
                    duration: 0
                },
                elements: {
                    line: {
                        tension: 0
                    }
                },
                responsive: true,
                maintainAspectRatio: false,
                legend: {
                    display: false
                },
                events: this.showAxis ? ['mousemove', 'mouseout', 'click'] : [],
                hover: {
                    animationDuration: 0,
                    mode: 'index',
                    intersect: false,
                    onHover: (event: MouseEvent, elements: {[key: string]: any}[]): void => {
                        if (event.type !== 'mouseout' && event.offsetX >= 25 && event.offsetX <= 167) {
                            this.showTooltip = true;
                            this.setTooltipData(elements);
                            this.setTooltipPosition(event);
                        } else {
                            // * event.type === 'mouseout'
                            this.showTooltip = false;
                        }
                    }
                },
                responsiveAnimationDuration: 0,
                tooltips: {
                    enabled: false,
                },
                scales: {
                    xAxes: [{
                        type: 'realtime',
                        gridLines: {
                            display: !this.showAxis,
                            drawBorder: false,
                            tickMarkLength: 0
                        },
                        ticks: {
                            display: false
                        }
                    }],
                    yAxes: [{
                        gridLines: {
                            display: this.showAxis,
                            drawBorder: false,
                            tickMarkLength: 0
                        },
                        ticks: {
                            display: this.showAxis,
                            beginAtZero: true,
                            min: 0,
                            max: this.defaultYMax,
                            padding: 5
                        },

                    }]
                },
                plugins: {
                    streaming: {
                        duration: 12000,    // data in the past 12000 ms will be displayed
                        // refresh: 1000,   // onRefresh callback will be called every 1000 ms
                        delay: 2000,        // delay of 2000 ms, so upcoming values are known before plotting a line
                        frameRate: 30,      // chart is drawn 30 times every second
                        // pause: false,
                    }
                }
            }
        });
    }

    private setTooltipData(elements: {[key: string]: any}[]): void {
        const datasets = this.chartObj.config.data.datasets;

        this.tooltipDataObj = {
            title: moment((datasets[0].data[elements[0]._index] as ChartPoint).x).tz(this.timezone).format(this.dateFormat),
            values: elements.map((element: {[key: string]: any}, i: number) => (datasets[i].data[element._index] as ChartPoint).y as number)
        };
    }

    private setTooltipPosition(event: MouseEvent): void {
        if (this.tooltip) {
            const tooltipCaret = this.tooltipCaret.nativeElement;
            const tooltip = this.tooltip.nativeElement;
            const ratio = event.offsetX / this.el.nativeElement.offsetWidth;

            this.renderer.setStyle(tooltipCaret, 'left', event.offsetX - (tooltipCaret.offsetWidth / 2) + 'px');
            this.renderer.setStyle(tooltip, 'left', event.offsetX - (tooltip.offsetWidth * ratio) + 'px');
        }
    }

    private updateChartData({timeStamp, responseCount}: {timeStamp: number, responseCount: number[]}): void {
        if (responseCount.length === 0) {
            this.chartObj.config.options.plugins.streaming.pause = true;
        } else {
            this.chartObj.config.data.datasets.forEach(function(dataset, i) {
                (dataset.data as ChartPoint[]).push({
                    x: timeStamp,
                    y: responseCount[i]
                });
            });
            /**
             * 1. 데이터 들어올때 마다 최댓값을 yMaxHolder에 넣음.
             * 2. 11초(duration time이랑 비슷한 시간) 지날때마다 맨앞의 element 제거.
             * 3. yMaxHolder의 최댓값을 yMax로 세팅.
             */
            // this.yMaxHolder.push(Math.max(...responseCount));
            // setTimeout(() => {
            //     this.yMaxHolder.shift();
            // }, 11000);
            // this.chartObj.config.options.scales.yAxes[0].ticks.max = Math.max(this.defaultYMax, Math.round(Math.max(...this.yMaxHolder) * 2));
            this.chartObj.config.options.scales.yAxes[0].ticks.max =
                Math.max(...this.chartObj.data.datasets.map(function(dataset: ChartDataSets) {
                    return Math.max(...(dataset.data as ChartPoint[]).map(function(value) {
                        return value.y as number;
                    }));
                })) >= this.defaultYMax ? undefined : this.defaultYMax;
            this.chartObj.config.options.plugins.streaming.pause = false;
        }
    }
}
