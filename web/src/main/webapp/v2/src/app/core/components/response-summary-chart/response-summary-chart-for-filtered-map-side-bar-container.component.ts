import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { filter } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { Actions } from 'app/shared/store';
import { StoreHelperService, WebAppSettingDataService, AgentHistogramDataService, AnalyticsService, TRACKED_EVENT_LIST, DynamicPopupService  } from 'app/shared/services';
import { ServerMapData } from 'app/core/components/server-map/class/server-map-data.class';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';

@Component({
    selector: 'pp-response-summary-chart-for-filtered-map-side-bar-container',
    templateUrl: './response-summary-chart-for-filtered-map-side-bar-container.component.html',
    styleUrls: ['./response-summary-chart-for-filtered-map-side-bar-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ResponseSummaryChartForFilteredMapSideBarContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    yMax = -1;
    selectedTarget: ISelectedTarget;
    selectedAgent = '';
    serverMapData: ServerMapData;
    hiddenComponent = false;
    hiddenChart = false;
    useDisable = false;
    showLoading = false;
    i18nText = {
        NO_DATA: ''
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
        this.translateService.get('COMMON.NO_DATA').subscribe((txt: string) => {
            this.i18nText.NO_DATA = txt;
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
            if (this.selectedTarget && this.selectedTarget.isMerged === false) {
                this.yMax = -1;
                this.loadResponseSummaryChartData();
            }
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
        if (this.selectedAgent === '') {
            this.passDownChartData(this.agentHistogramDataService.makeChartDataForResponseSummary(target.histogram, this.getChartYMax()));
        } else {
            this.passDownChartData(this.agentHistogramDataService.makeChartDataForResponseSummary(target['agentHistogram'][this.selectedAgent], this.getChartYMax()));
        }
    }
    private passDownChartData(chartData: any): void {
        if (chartData) {
            this.hiddenChart = false;
            this.chartData = chartData;
        } else {
            this.hiddenChart = true;
        }
        this.setDisable(false);
        this.changeDetector.detectChanges();
    }
    private getTargetInfo(): any {
        if (this.selectedTarget.isNode) {
            return this.serverMapData.getNodeData(this.selectedTarget.node[0]);
        } else {
            // return this.serverMapData.getNodeData(this.serverMapData.getLinkData(this.selectedTarget.link[0]).to);
            return this.serverMapData.getLinkData(this.selectedTarget.link[0]);
        }
    }
    onNotifyMax(max: number): void {
        if (max > this.yMax) {
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
