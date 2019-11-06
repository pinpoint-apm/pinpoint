import { Component, OnInit, OnDestroy, Input, ViewChild, ElementRef } from '@angular/core';
import { Chart } from 'chart.js';

@Component({
    selector: 'pp-agent-admin-chart',
    templateUrl: './agent-admin-chart.component.html',
    styleUrls: ['./agent-admin-chart.component.css']
})
export class AgentAdminChartComponent implements OnInit, OnDestroy  {
    @ViewChild('agentChart', { static: true }) el: ElementRef;
    _chartData: any;
    chartObj: Chart;
    @Input() type: string;
    @Input() title: string;
    @Input() barColor: string;
    @Input()
    set chartData(value: any) {
        if (value && value.jvmVersion && value.agentVersion) {
            this._chartData = value;
            this.initChartObj();
        }
    }
    constructor() {}
    ngOnInit() {}
    ngOnDestroy() {}
    private initChartObj() {
        this.chartObj = new Chart(this.el.nativeElement.getContext('2d'), {
            type: 'horizontalBar',
            data: this.makeDataOption(),
            options: this.makeNormalOption()
        });
    }
    private makeDataOption(): any {
        const labels: string[] = [];
        const values: number[] = [];
        const dataSet = this._chartData[this.type];
        Object.keys(dataSet).sort().forEach((key: string) => {
            labels.push(key);
            values.push(dataSet[key]);
        });
        const dataOption = {
            labels: labels,
            borderWidth: 0,
            datasets: []
        };
        dataOption.datasets.push({
            label: this.title,
            data: values,
            backgroundColor: this.barColor,
            borderWidth: 0
        });
        return dataOption;
    }
    private makeNormalOption(): any {
        return {
            maintainAspectRatio: false,
            tooltips: {
                enabled: false
            },
            scales: {
                yAxes: [{
                    gridLines: {
                        display: false
                    },
                    ticks: {
                        fontFamily: 'monospace'
                    }
                }],
                xAxes: [{
                    ticks: {
                        fontFamily: 'monospace'
                    }
                }]
            },
            animation: {
                duration: 0,
                onComplete: (chartElement: any) => {
                    const ctx = chartElement.chart.ctx;
                    ctx.fillStyle = chartElement.chart.config.options.defaultFontColor;
                    ctx.fontSize = 9;
                    ctx.textAlign = 'left';
                    ctx.textBaseline = 'top';
                    chartElement.chart.data.datasets.forEach((dataset: any) => {
                        for (let i = 0 ; i < dataset.data.length ; i++) {
                            const model = dataset._meta[Object.keys(dataset._meta)[0]].data[i]._model;
                            ctx.fillText(dataset.data[i], model.x, model.y - 5);
                        }
                    });
                }
            },
            hover: {
                animationDuration: 0
            },
            legend: {
                display: true,
                labels: {
                    boxWidth: 30,
                    padding: 10,
                    fontFamily: 'monospace'
                },
                position: 'bottom'
            }
        };
    }
}
