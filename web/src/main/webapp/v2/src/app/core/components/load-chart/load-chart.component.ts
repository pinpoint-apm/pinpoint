import { Component, OnInit, OnChanges, ViewChild, ElementRef, SimpleChanges, Input, Output, EventEmitter } from '@angular/core';
import { Chart } from 'chart.js';

// @TODO Loading 전 화면 처리
@Component({
    selector: 'pp-load-chart',
    templateUrl: './load-chart.component.html',
    styleUrls: ['./load-chart.component.css']
})
export class LoadChartComponent implements OnInit, OnChanges {
    @ViewChild('loadChart') el: ElementRef;
    @Input() chartData: any;
    @Input() chartColors: string[];
    @Output() outNotifyMax: EventEmitter<number> = new EventEmitter();
    @Output() outClickColumn: EventEmitter<string> = new EventEmitter();
    chartObj: any;
    constructor() {}
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        if (changes['chartData'] && changes['chartData']['firstChange'] === false) {
            this.initChart();
        }
    }
    private initChart(): void {
        if (this.chartObj) {
            if (this.chartData.max) {
                this.chartObj.config.options.scales.yAxes[0].ticks.max = this.chartData.max;
            } else {
                delete(this.chartObj.config.options.scales.yAxes[0].ticks.max);
            }
            this.chartObj.data.labels = this.chartData.labels;
            this.chartData.keyValues.forEach((keyValues: any, index: number) => {
                this.chartObj.data.datasets[index].data = keyValues.values;
                this.chartObj.data.datasets[index].label = keyValues.key;
            });
            this.chartObj.update();
        } else {
            this.chartObj = new Chart(this.el.nativeElement.getContext('2d'), {
                type: 'bar',
                data: this.makeDataOption(),
                options: this.makeNormalOption()
            });
        }
        this.outNotifyMax.emit(this.chartObj.scales['y-axis-0'].max);
    }
    private makeDataOption(): any {
        const dataOption = {
            labels: this.chartData.labels,
            borderWidth: 0,
            datasets: []
        };

        this.chartData.keyValues.forEach((keyValues: any, index: number) => {
            dataOption.datasets.push({
                label: keyValues.key,
                data: keyValues.values,
                backgroundColor: this.chartColors[index],
                borderColor: 'rgba(120, 119, 121, 0.8)',
                borderWidth: 0
            });
        });
        return dataOption;
    }
    private makeNormalOption(): any {
        return {
            onClick: (event: any, aChartEl: any[]) => {
                if ( aChartEl.length > 0 ) {
                    this.outClickColumn.emit(aChartEl[0]._view.label);
                }
                event.preventDefault();
                // AnalyticsService.send(AnalyticsService.CONST.MAIN, AnalyticsService.CONST.CLK_LOAD_GRAPH);
            },
            maintainAspectRatio: false,
            tooltips: {
                mode: 'label',
                bodySpacing: 6
            },
            scales: {
                yAxes: [{
                    gridLines: {
                        display: true,
                        drawBorder: false,
                        zeroLineWidth: 1.5,
                        zeroLineColor: 'rgb(0, 0, 0)'
                    },
                    ticks: {
                        beginAtZero: true,
                        maxTicksLimit: 4,
                        callback: (label: number) => {
                            return '   ' + (label >= 1000 ? `${label / 1000}k` : label) + '  ';
                        },
                        fontColor: 'rgba(162, 162, 162, 1)',
                        fontSize: 11,
                        max: this.chartData.max
                    },
                    stacked: true
                }],
                xAxes: [{
                    gridLines: {
                        display: false,
                        drawBorder: false
                    },
                    ticks: {
                        maxTicksLimit: 6,
                        callback: (label: string): string[] => {
                            return label.split('#');
                        },
                        autoSkip: true,
                        fontColor: 'rgba(162, 162, 162, 1)',
                        fontSize: 11,
                        max: this.chartData.max
                    },
                    categoryPercentage: 1.0,
                    barPercentage: 1.0,
                    stacked: true,
                    display: true
                }]
            },
            animation: {
                duration: 0
            },
            legend: {
                display: true,
                labels: {
                    boxWidth: 30,
                    padding: 10
                },
                position: 'bottom'
            }
        };
    }
    getPreSpace(str: string) {
        const space = '       '; // 7 is max space
        if (str.length > space.length) {
            return str;
        } else {
            return space.substr(0, space.length - str.length);
        }
    }
}
