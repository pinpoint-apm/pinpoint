import { Component, OnInit, OnChanges, ViewChild, ElementRef, SimpleChanges, Input, Output, EventEmitter, HostBinding } from '@angular/core';
import bb, { PrimitiveArray } from 'billboard.js';

@Component({
    selector: 'pp-url-statistic-chart',
    templateUrl: './url-statistic-chart.component.html',
    styleUrls: ['./url-statistic-chart.component.css']
})
export class UrlStatisticChartComponent implements OnInit, OnChanges {
    @HostBinding('class') hostClass = 'l-url-statistic-chart';
    @ViewChild('chartHolder', { static: true }) chartHolder: ElementRef;
    @Input() chartConfig: IChartConfig;
    @Output() outRendered = new EventEmitter<void>(true);

    private chartInstance: any;

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
                    case 'chartConfig':
                        this.chartInstance ? this.updateChart(changedProp) : this.initChart();
                        break;
                }
            });
    }

    private initChart(): void {
        this.chartInstance = bb.generate({
            bindto: this.chartHolder.nativeElement,
            data: this.chartConfig.dataConfig,
            ...this.chartConfig.elseConfig,
            onrendered: () => this.finishLoading(),
        });
    }

    private updateChart({previousValue, currentValue}: {previousValue: IChartConfig, currentValue: IChartConfig}): void {
        const {columns: currColumns} = currentValue.dataConfig;
        const currKeys = currColumns.map(([key]: PrimitiveArray) => key);
        const {axis: {y}} = currentValue.elseConfig;

        const unload = currKeys.length === 0;

        this.chartInstance.config('data.empty.label.text', currentValue.dataConfig.empty.label.text);
        if (unload) {
            this.chartInstance.config('axis.y.padding', {top: 0, bottom: 0});
            this.chartInstance.config('axis.y.max', y.default[1]);
            this.chartInstance.config('axis.y.tick.count', y.tick.count);
            this.chartInstance.unload();
            this.chartInstance.flush();
        } else {
            this.chartInstance.config('axis.y.padding', {top: null, bottom: 0});
            this.chartInstance.config('axis.y.max', null);
            this.chartInstance.config('axis.y.tick.count', null);

            this.chartInstance.load({
                columns: currColumns,
            });
        }
    }

    private finishLoading(): void {
        this.outRendered.emit();
    }
}
