import { Component, ViewChild, ElementRef, OnInit, OnChanges, SimpleChanges, Input, Output, EventEmitter } from '@angular/core';
import { Chart } from 'chart.js';

@Component({
    selector: 'pp-response-summary-chart',
    templateUrl: './response-summary-chart.component.html',
    styleUrls: ['./response-summary-chart.component.css']
})
export class ResponseSummaryChartComponent implements OnInit, OnChanges {
    @ViewChild('responseSummaryChart') el: ElementRef;
    @Input() instanceKey: string;
    @Input() chartData: any;
    @Input() chartColors: string[];
    @Output() outNotifyMax: EventEmitter<number> = new EventEmitter();
    @Output() outClickColumn: EventEmitter<string> = new EventEmitter();
    chartObj: any;
    constructor() {}
    ngOnInit() {
    }
    ngOnChanges(changes: SimpleChanges) {
        if ( changes['chartData'] && changes['chartData']['firstChange'] === false ) {
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
            this.chartObj.data.labels = this.chartData.keys;
            this.chartObj.data.datasets[0].data = this.chartData.values;
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
        return {
            labels: this.chartData['keys'],
            datasets: [{
                data: this.chartData['values'],
                backgroundColor: this.chartColors,
                borderColor: [
                    'rgba(120, 119, 121, 0)',
                    'rgba(120, 119, 121, 0)',
                    'rgba(120, 119, 121, 0)',
                    'rgba(120, 119, 121, 0)',
                    'rgba(120, 119, 121, 0)'
                ],
                borderWidth: 0.5
            }]
        };
    }
    private makeNormalOption(): any {
        return {
            onClick: (event, aChartEl: any[]) => {
                if ( aChartEl.length > 0 ) {
                    this.outClickColumn.emit(aChartEl[0]._view.label);
                }
                event.preventDefault();
            },
            layout: {
                padding: {
                    top: 20
                }
            },
            maintainAspectRatio: false,
            legend: {
                display: false
            },
            title: {
                display: false
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
                        maxTicksLimit: 3,
                        callback: (label: number) => {
                            return '   ' + (label >= 1000 ? `${label / 1000}k` : label) + '  ';
                        },
                        fontColor: 'rgba(162, 162, 162, 1)',
                        fontSize: 11,
                        max: this.chartData.max
                    }
                }],
                xAxes: [{
                    gridLines: {
                        display: false,
                        drawBorder: false
                    },
                    ticks: {
                        fontColor: 'rgba(162, 162, 162, 1)',
                        fontSize: 11
                    }
                }]
            },
            animation: {
                duration: 0,
                onComplete: (chartElement: any) => {
                    const ctx = chartElement.chart.ctx;
                    // ctx.font = Chart.helpers.fontString(Chart.defaults.global.defaultFontSize, 'normal', Chart.defaults.global.defaultFontFamily);
                    ctx.fillStyle = chartElement.chart.config.options.defaultFontColor;
                    ctx.textAlign = 'center';
                    ctx.textBaseline = 'bottom';
                    chartElement.chart.data.datasets.forEach((dataset) => {
                        for (let i = 0 ; i < dataset.data.length ; i++) {
                            const model = dataset._meta[Object.keys(dataset._meta)[0]].data[i]._model;
                            ctx.fillText(this.addComma(dataset.data[i] + ''), model.x, model.y - 5);
                        }
                    });
                }
            },
            hover: {
                animationDuration: 0
            },
            tooltips: {
                enabled: false
            }
        };
    }
    addComma(str: string): string {
        return str.replace(/(\d)(?=(?:\d{3})+(?!\d))/g, '$1,');
    }
}
