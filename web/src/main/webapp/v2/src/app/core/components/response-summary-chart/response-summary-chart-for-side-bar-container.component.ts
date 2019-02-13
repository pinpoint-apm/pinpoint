import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject, combineLatest } from 'rxjs';
import { filter } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { Actions } from 'app/shared/store';
import { ServerMapData } from 'app/core/components/server-map/class/server-map-data.class';
import { StoreHelperService, WebAppSettingDataService, AgentHistogramDataService, AnalyticsService, TRACKED_EVENT_LIST, DynamicPopupService } from 'app/shared/services';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';

@Component({
    selector: 'pp-response-summary-chart-for-side-bar-container',
    templateUrl: './response-summary-chart-for-side-bar-container.component.html',
    styleUrls: ['./response-summary-chart-for-side-bar-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ResponseSummaryChartForSideBarContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    hasRequestError = false;
    selectedTarget: ISelectedTarget;
    selectedAgent = '';
    serverMapData: ServerMapData;
    hiddenComponent = false;
    hiddenChart = false;
    yMax = -1;
    useDisable = false;
    showLoading = false;
    i18nText = {
        NO_DATA: '',
        FAILED_TO_FETCH_DATA: ''
    };
    chartData: IResponseTime | IResponseMilliSecondTime;
    chartColors: string[];
    constructor(
        private changeDetector: ChangeDetectorRef,
        private translateService: TranslateService,
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService,
        private agentHistogramDataService: AgentHistogramDataService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService
    ) {}
    ngOnInit() {
        this.chartColors = this.webAppSettingDataService.getColorByRequest();
        combineLatest(
            this.translateService.get('COMMON.NO_DATA'),
            this.translateService.get('COMMON.FAILED_TO_FETCH_DATA')
        ).subscribe((text: string[]) => {
            this.i18nText = {
                NO_DATA: text[0],
                FAILED_TO_FETCH_DATA: text[1]
            };
        });
        this.connectStore();
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private connectStore(): void {
        this.storeHelperService.getAgentSelection(this.unsubscribe).subscribe((agent: string) => {
            this.setDisable(true);
            this.selectedAgent = agent;
            if (this.selectedTarget) {
                this.loadResponseSummaryChartData();
            }
            this.changeDetector.detectChanges();
        });
        this.storeHelperService.getServerMapData(this.unsubscribe).subscribe((serverMapData: ServerMapData) => {
            this.serverMapData = serverMapData;
        });
        this.storeHelperService.getServerMapTargetSelected(this.unsubscribe).pipe(
            filter((target: ISelectedTarget) => {
                return target && (target.isNode === true || target.isNode === false) ? true : false;
            })
        ).subscribe((target: ISelectedTarget) => {
            this.yMax = -1;
            this.selectedAgent = '';
            this.selectedTarget = target;
            this.hiddenComponent = target.isMerged;
            if (target.isMerged === false) {
                this.loadResponseSummaryChartData();
            }
            this.changeDetector.detectChanges();
        });
        this.storeHelperService.getRealTimeScatterChartRange(this.unsubscribe).subscribe((range: IScatterXRange) => {
            this.yMax = -1;
            this.loadResponseSummaryChartData(range.from, range.to);
        });
        this.storeHelperService.getServerMapTargetSelectedByList(this.unsubscribe).subscribe((target: any) => {
            this.yMax = -1;
            this.hiddenComponent = false;
            this.passDownChartData(this.agentHistogramDataService.makeChartDataForResponseSummary(target.histogram, this.getChartYMax()));
        });
    }
    private getChartYMax(): number {
        return this.yMax === -1 ? null : this.yMax;
    }
    private setDisable(disable: boolean): void {
        this.useDisable = disable;
        this.showLoading = disable;
    }
    private loadResponseSummaryChartData(from?: number, to?: number): void {
        const target = this.getTargetInfo();
        if (target) {
            if (this.isAllAgent() && arguments.length !== 2) {
                this.passDownChartData(this.agentHistogramDataService.makeChartDataForResponseSummary(target.histogram, this.getChartYMax()));
            } else {
                this.agentHistogramDataService.getData(target.key, target.applicationName, target.serviceTypeCode, this.serverMapData, from, to).subscribe((chartData: any) => {
                    const chartDataForAgent = this.isAllAgent() ? chartData['histogram'] : chartData['agentHistogram'][this.selectedAgent];
                    this.passDownChartData(this.agentHistogramDataService.makeChartDataForResponseSummary(chartDataForAgent, this.getChartYMax()));
                }, (error: IServerErrorFormat) => {
                    this.hasRequestError = true;
                    this.hiddenChart = true;
                    this.setDisable(false);
                    this.changeDetector.detectChanges();
                });
            }
        }
    }
    private passDownChartData(chartData: any): void {
        if (chartData) {
            this.hiddenChart = false;
            this.chartData = chartData;
        } else {
            this.hiddenChart = true;
        }
        this.hasRequestError = false;
        this.setDisable(false);
        this.changeDetector.detectChanges();
    }
    private getTargetInfo(): any {
        if (this.selectedTarget.isNode) {
            return this.serverMapData.getNodeData(this.selectedTarget.node[0]);
        } else {
            return this.serverMapData.getLinkData(this.selectedTarget.link[0]);
        }
    }
    private isAllAgent(): boolean {
        return this.selectedAgent === '';
    }
    getChartMessage(): string {
        return this.hasRequestError ? this.i18nText.FAILED_TO_FETCH_DATA : this.i18nText.NO_DATA;
    }
    onNotifyMax(max: number): void {
        if (this.yMax === -1) {
            this.yMax = max;
            this.storeHelperService.dispatch(new Actions.ChangeResponseSummaryChartYMax(max));
        }
    }
    onClickColumn(columnName: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_RESPONSE_GRAPH);
        console.log('clicked Column Name :', columnName );
        if (columnName === 'Error') {
            // scope.$emit('responseTimeSummaryChartDirective.showErrorTransactionList', type);
            // @TODO Scatter Chart의 에러 부분만 Drag 하도록 하는 액션
        }
        // @TODO FilteredMap transaction에서 만 처리되는 이벤트
        // if (useFilterTransaction) {
        //     scope.$emit('responseTimeSummaryChartDirective.itemClicked.' + scope.namespace, {
        //         "responseTime": type,
        //         "count": aTarget[0]._chart.config.data.datasets[0].data[aTarget[0]._index]
        //     });
        // }

    }
    onShowHelp($event: MouseEvent): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.TOGGLE_HELP_VIEWER, HELP_VIEWER_LIST.RESPONSE_SUMMARY);
        const {left, top, width, height} = ($event.target as HTMLElement).getBoundingClientRect();

        this.dynamicPopupService.openPopup({
            data: HELP_VIEWER_LIST.RESPONSE_SUMMARY,
            coord: {
                coordX: left + width / 2,
                coordY: top + height / 2
            },
            component: HelpViewerPopupContainerComponent
        });
    }
}
