import { Component, ElementRef, EventEmitter, Input, OnInit, Output, Renderer2, SimpleChanges, ViewChild, OnChanges, OnDestroy } from '@angular/core';
import { takeUntil, filter } from 'rxjs/operators';
import bb, { PrimitiveArray } from 'billboard.js';

import { MessageQueueService, MESSAGE_TO } from 'app/shared/services';
import { Subject, merge, fromEvent } from 'rxjs';

@Component({
    selector: 'pp-metric',
    templateUrl: './metric.component.html',
    styleUrls: ['./metric.component.css']
})
export class MetricComponent implements OnInit, OnChanges, OnDestroy {
    @ViewChild('chartHolder', {static: true}) chartHolder: ElementRef;
    @Input() chartConfig: IChartConfig;
    @Output() outRendered = new EventEmitter<void>(true);

    private unsubscribe = new Subject<void>();
    private chartInstance: any;
    private readonly ratio = 1.92;
    // private readonly ratio = 4.69;

    constructor(
        private messageQueueService: MessageQueueService,
        private el: ElementRef,
        private renderer: Renderer2
    ) {}

    ngOnInit() {
        this.setHeight();
        merge(
            fromEvent(window, 'resize'),
            // this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SET_CHART_LAYOUT).pipe(),
        ).pipe(
            takeUntil(this.unsubscribe),
            filter(() => this.chartInstance && this.el.nativeElement.isConnected)
        ).subscribe(() => {
            this.resize();
        });
    }

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

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    resize(): void {
        this.setHeight();
        this.chartInstance.resize();
        this.setViewBox();
    }

    private setHeight(): void {
        const width = getComputedStyle(this.chartHolder.nativeElement).getPropertyValue('width');
        const height = `${Number(width.replace(/px/, '')) / this.ratio}px`;

        this.renderer.setStyle(this.chartHolder.nativeElement, 'height', height);
    }

    private updateChart({previousValue, currentValue}: {previousValue: IChartConfig, currentValue: IChartConfig}): void {
        const {columns: prevColumns} = previousValue.dataConfig;
        const {columns: currColumns} = currentValue.dataConfig;
        const prevKeys = prevColumns.map(([key]: PrimitiveArray) => key);
        const currKeys = currColumns.map(([key]: PrimitiveArray) => key);
        const removedKeys = prevKeys.filter((key: string) => !currKeys.includes(key));
        const unloadKeys = [...this.getEmptyDataKeys(currColumns), ...removedKeys];
        // const {axis: {y}} = currentValue.elseConfig;

        // this.chartInstance.config('axis.y.max', y.max);
        this.chartInstance.load({
            columns: currColumns,
            unload: unloadKeys,
        });
    }

    private getEmptyDataKeys(data: PrimitiveArray[]): string[] {
        return data.slice(1).filter((d: PrimitiveArray) => d.length === 1).map(([key]: PrimitiveArray) => key as string);
    }

    private setViewBox(): void {
        const svg = this.el.nativeElement.querySelector('svg');
        const width = svg.getAttribute('width');
        const height = svg.getAttribute('height');

        this.renderer.setStyle(svg, 'width', `${width}px`);
        this.renderer.setStyle(svg, 'height', `${height}px`);
        this.renderer.setAttribute(svg, 'viewBox', `0 0 ${width} ${height}`);
        this.renderer.setAttribute(svg, 'preserveAspectRatio', `none`);
    }

    private initChart(): void {
        this.chartInstance = bb.generate({
            bindto: this.chartHolder.nativeElement,
            data: this.chartConfig.dataConfig,
            ...this.chartConfig.elseConfig,
            onrendered: () => this.finishLoading(),
        });

        const svg = this.el.nativeElement.querySelector('svg');

        this.renderer.setStyle(svg, 'transition', 'all ease 1s');
    }

    private finishLoading(): void {
        this.outRendered.emit();
    }
}
