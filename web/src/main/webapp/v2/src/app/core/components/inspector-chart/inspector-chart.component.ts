import { Component, OnInit, OnChanges, Input, Output, EventEmitter, ViewChild, ElementRef, SimpleChanges, ChangeDetectorRef } from '@angular/core';
import { Chart } from 'chart.js';

export interface IChartConfig {
    type: string;
    dataConfig: {[key: string]: any};
    elseConfig: {[key: string]: any};
    isDataEmpty: boolean;
}

export interface II18nText {
    FAILED_TO_FETCH_DATA: string;
    NO_DATA_COLLECTED: string;
}

export interface IErrObj {
    errType: string; // EXCEPTION or ELSE
    errMessage: string; // data.exception.message or null
}

@Component({
    selector: 'pp-inspector-chart',
    templateUrl: './inspector-chart.component.html',
    styleUrls: ['./inspector-chart.component.css']
})
export class InspectorChartComponent implements OnInit, OnChanges {
    @ViewChild('chartElement') el: ElementRef;
    @Input() xRawData: number[];
    @Input() chartConfig: IChartConfig;
    @Input() i18nText: II18nText;
    @Input() errObj: IErrObj;
    @Input() hoveredInfo: IHoveredInfo;
    @Input() height: string;
    @Output() outRetryGetChartData: EventEmitter<void> = new EventEmitter();

    retryMessage: string;
    chartVisibility = {};
    chartSectionLayers = {
        loading: true,
        chart: false,
        retry: false
    };

    private chartObj: any;

    constructor(
        private changeDetector: ChangeDetectorRef,
    ) {}

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
                        this.setChartSectionVisibility('loading');
                        this.initChart();
                        break;
                    case 'errObj':
                        this.setChartSectionVisibility('loading');
                        this.setRetryMessage(changedProp.currentValue);
                        this.setChartSectionVisibility('retry');
                        break;
                    case 'hoveredInfo':
                        this.syncHoverOnChart(changedProp.currentValue);
                        break;
                }
            });
    }

    getHeightConfig(): {[key: string]: any} {
        return {
            height: this.height,
            setHeightAuto: this.chartSectionLayers.chart,
            ratio: 1.92
        };
    }

    private syncHoverOnChart(hoverInfo: IHoveredInfo): void {
        let activeElements;
        if (hoverInfo.index === -1) {
            if (hoverInfo.time) {
                activeElements = this.getActiveTooltipElementsByTime(hoverInfo.time);
            } else {
                activeElements = [];
            }
        } else {
            activeElements = this.getActiveTooltipElements(hoverInfo.index);
        }

        this.chartObj.tooltip._active = activeElements;
        this.chartObj.tooltip.update(true);
        this.chartObj.draw();
    }
    getActiveTooltipElementsByTime(time: number): any[] {
        let index = -1;
        const len = this.xRawData.length;
        for (let i = 0 ; i < len ; i++) {
            const t = this.xRawData[i];
            if (t === time) {
                index = i;
                break;
            } else if (t > time) {
                if (i + 1 === len) {
                    index = i;
                } else {
                    if (this.xRawData[i] - time >= this.xRawData[i - 1] - time) {
                        index = i - 1;
                    } else {
                        index = i;
                    }
                }
                break;
            }
        }
        return this.getActiveTooltipElements(index);
    }
    getActiveTooltipElements(index: number): any[] {
        return this.chartObj.data.datasets.map((val: any, i: number) => this.chartObj.getDatasetMeta(i).data[index]);
    }

    private initChart(): void {
        if (this.chartObj) {
            this.chartObj.data = this.chartConfig.dataConfig;
            this.chartObj.options.tooltips.callbacks.label = this.chartConfig.elseConfig.tooltips.callbacks.label;
            this.chartObj.options.scales.yAxes[0].ticks.max = this.chartConfig.elseConfig.scales.yAxes[0].ticks.max;
            this.chartObj.update();
        } else {
            this.chartObj = new Chart(this.el.nativeElement.getContext('2d'), {
                type: this.chartConfig.type,
                data: this.chartConfig.dataConfig,
                options: this.chartConfig.elseConfig,
                plugins: [{
                    afterRender: (chart, options) => {
                        this.finishLoading();
                    }
                }],
            });
        }
    }

    private finishLoading(): void {
        this.setChartSectionVisibility('chart');
    }

    private setRetryMessage(errObj: IErrObj): void {
        this.retryMessage = errObj.errType === 'EXCEPTION' ? errObj.errMessage : this.i18nText['FAILED_TO_FETCH_DATA'];
    }

    private setChartSectionVisibility(layer: string): void {
        this.setChartSectionLayerAs(layer);
        this.setChartVisibility(this.chartSectionLayers['chart'], this.chartObj);
        this.notifyChanges();
    }

    private setChartSectionLayerAs(whichLayerToShow: string): void {
        Object.keys(this.chartSectionLayers).forEach((layer) => {
            this.chartSectionLayers[layer] = whichLayerToShow === layer;
        });
    }

    private setChartVisibility(showChart: boolean, chartObj: Chart): void {
        this.chartVisibility = {
            'show-chart': showChart,
            'shady-chart': !showChart && chartObj !== undefined,
            'hide-chart': !showChart && chartObj === undefined
        };
    }

    retryGetChartData(): void {
        this.setChartSectionVisibility('loading');
        this.outRetryGetChartData.emit();
    }

    showNoData(): boolean {
        return this.chartSectionLayers.chart && this.chartConfig.isDataEmpty;
    }

    private notifyChanges(): void {
        if (!this.changeDetector['destroyed']) {
            this.changeDetector.detectChanges();
        }
    }
}
