import { Component, OnInit, ViewChild, ElementRef, SimpleChanges, Input, Output, EventEmitter, HostBinding } from '@angular/core';
import bb, { Chart, bar } from 'billboard.js';

@Component({
    selector: 'pp-url-statistic-default-chart',
    templateUrl: './url-statistic-chart.component.html',
    styleUrls: ['./url-statistic-chart.component.css']
})
export class UrlStatisticDefaultChartComponent implements OnInit {
    @HostBinding('class') hostClass = 'l-url-statistic-chart';
    @ViewChild('chartHolder', { static: true }) chartHolder: ElementRef;
    @Input() emptyMessage: string;
    @Output() outRendered = new EventEmitter<void>(true);

    private chartInstance: Chart;
    private defaultYMax = 1;

    constructor() {}
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        this.chartInstance = bb.generate({
            bindto: this.chartHolder.nativeElement,
            data: {
                x: 'x',
                columns: [],
                empty: {
                    label: {
                        text: this.emptyMessage
                    }
                },
                type: bar()
            },
            ...this.getChartOption(),
            onrendered: () => this.finishLoading(),
        });
    }

    private getChartOption(): {[key: string]: any} {
        return {
            padding: {
                top: 20,
                bottom: 20,
                right: 20
            },
            axis: {
                y: {
                    tick: {
                        count: 2,
                    },
                    padding: {
                        top: 0,
                        bottom: 0
                    },
                    min: 0,
                    default: [0, this.defaultYMax]
                }
            },
        }
    }

    private finishLoading(): void {
        this.outRendered.emit();
    }
}
