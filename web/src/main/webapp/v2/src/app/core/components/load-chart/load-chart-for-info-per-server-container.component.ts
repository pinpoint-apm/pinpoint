import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { filter } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { WebAppSettingDataService, StoreHelperService, AgentHistogramDataService, AnalyticsService, TRACKED_EVENT_LIST, DynamicPopupService } from 'app/shared/services';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';

@Component({
    selector: 'pp-load-chart-for-info-per-server-container',
    templateUrl: './load-chart-for-info-per-server-container.component.html',
    styleUrls: ['./load-chart-for-info-per-server-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoadChartForInfoPerServerContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    private lastChartData: any = null;
    private timezone: string;
    private dateFormatMonth: string;
    private dateFormatDay: string;
    hiddenChart = false;
    yMax: number;
    chartData: IHistogram[];
    chartColors: string[];
    i18nText = {
        NO_DATA: ''
    };
    useDisable = false;
    showLoading = false;
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
        this.storeHelperService.getTimezone(this.unsubscribe).subscribe((timezone: string) => {
            this.timezone = timezone;
            if (this.lastChartData) {
                this.makeChartData();
            }
        });
        this.storeHelperService.getDateFormatArray(this.unsubscribe, 5, 6).subscribe((dateFormat: string[]) => {
            this.dateFormatMonth = dateFormat[0];
            this.dateFormatDay = dateFormat[1];
            if (this.lastChartData) {
                this.makeChartData();
            }
        });
        this.storeHelperService.getLoadChartYMax(this.unsubscribe).subscribe((max: number) => {
            this.yMax = max;
        });
        this.storeHelperService.getAgentSelectionForServerList(this.unsubscribe).pipe(
            filter((chartData: IAgentSelection) => {
                return (chartData && chartData.agent) ? true : false;
            })
        ).subscribe((chartData: IAgentSelection) => {
            this.lastChartData = chartData;
            this.makeChartData();
        });
    }
    private makeChartData(): void {
        if (this.lastChartData.load) {
            this.hiddenChart = false;
            this.chartData = this.agentHistogramDataService.makeChartDataForLoad(this.lastChartData.load, this.timezone, [this.dateFormatMonth, this.dateFormatDay], this.yMax);
        } else {
            this.hiddenChart = true;
        }
        this.changeDetector.detectChanges();
    }
    onNotifyMax(max: number): void {}
    onClickColumn($event: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_LOAD_GRAPH);
    }
    onShowHelp($event: MouseEvent): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.TOGGLE_HELP_VIEWER, HELP_VIEWER_LIST.LOAD);
        const {left, top, width, height} = ($event.target as HTMLElement).getBoundingClientRect();

        this.dynamicPopupService.openPopup({
            data: HELP_VIEWER_LIST.LOAD,
            coord: {
                coordX: left + width / 2,
                coordY: top + height / 2
            },
            component: HelpViewerPopupContainerComponent
        });
    }
}
