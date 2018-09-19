import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { filter } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { WebAppSettingDataService, StoreHelperService, AgentHistogramDataService, AnalyticsService, TRACKED_EVENT_LIST, DynamicPopupService } from 'app/shared/services';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';

@Component({
    selector: 'pp-response-summary-chart-for-info-per-server-container',
    templateUrl: './response-summary-chart-for-info-per-server-container.component.html',
    styleUrls: ['./response-summary-chart-for-info-per-server-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ResponseSummaryChartForInfoPerServerContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    hiddenChart = false;
    yMax: number;
    chartData: IResponseTime | IResponseMilliSecondTime;
    chartColors: string[];
    useDisable = false;
    showLoading = false;
    i18nText = {
        NO_DATA: ''
    };
    constructor(
        private changeDetector: ChangeDetectorRef,
        private storeHelperService: StoreHelperService,
        private translateService: TranslateService,
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
        this.storeHelperService.getResponseSummaryChartYMax(this.unsubscribe).subscribe((max: number) => {
            this.yMax = max;
        });
        this.storeHelperService.getAgentSelectionForServerList(this.unsubscribe).pipe(
            filter((chartData: IAgentSelection) => {
                return (chartData && chartData.agent) ? true : false;
            })
        ).subscribe((chartData: IAgentSelection) => {
            if (chartData.responseSummary) {
                this.hiddenChart = false;
                this.chartData = this.agentHistogramDataService.makeChartDataForResponseSummary(chartData.responseSummary, this.yMax);
            } else {
                this.hiddenChart = true;
            }
            this.changeDetector.detectChanges();
        });
    }
    onNotifyMax(max: number): void {}
    onClickColumn(columnName: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_RESPONSE_GRAPH);
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
