import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { filter } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { Actions } from 'app/shared/store';
import { WebAppSettingDataService, StoreHelperService, AgentHistogramDataService, AnalyticsService, TRACKED_EVENT_LIST, DynamicPopupService } from 'app/shared/services';
import { ServerMapData } from 'app/core/components/server-map/class/server-map-data.class';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';

@Component({
    selector: 'pp-load-chart-for-filtered-map-side-bar-container',
    templateUrl: './load-chart-for-filtered-map-side-bar-container.component.html',
    styleUrls: ['./load-chart-for-filtered-map-side-bar-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoadChartForFilteredMapSideBarContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    private timezone: string;
    private dateFormatMonth: string;
    private dateFormatDay: string;
    hiddenComponent = false;
    hiddenChart = false;
    yMax = -1;
    selectedTarget: ISelectedTarget;
    selectedAgent = '';
    serverMapData: ServerMapData;
    useDisable = false;
    showLoading = false;
    i18nText = {
        NO_DATA: ''
    };
    chartData: IHistogram[];
    chartColors: string[];
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
            if (this.chartData) {
                this.loadLoadChartData();
            }
        });
        this.storeHelperService.getDateFormatArray(this.unsubscribe, 5, 6).subscribe((dateFormat: string[]) => {
            this.dateFormatMonth = dateFormat[0];
            this.dateFormatDay = dateFormat[1];
            if (this.chartData) {
                this.loadLoadChartData();
            }
        });
        this.storeHelperService.getAgentSelection(this.unsubscribe).subscribe((agent: string) => {
            this.setDisable(true);
            this.selectedAgent = agent;
            if (this.selectedTarget) {
                this.loadLoadChartData();
            }
            this.changeDetector.detectChanges();
        });
        this.storeHelperService.getServerMapData(this.unsubscribe).subscribe((serverMapData: ServerMapData) => {
            this.serverMapData = serverMapData;
            if (this.selectedTarget && this.selectedTarget.isMerged === false) {
                this.yMax = -1;
                this.loadLoadChartData();
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
                this.loadLoadChartData();
            }
            this.changeDetector.detectChanges();
        });
        this.storeHelperService.getServerMapTargetSelectedByList(this.unsubscribe).subscribe((target: any) => {
            this.yMax = -1;
            this.hiddenComponent = false;
            this.passDownChartData(this.agentHistogramDataService.makeChartDataForLoad(target.timeSeriesHistogram, this.timezone, [this.dateFormatMonth, this.dateFormatDay], this.getChartYMax()));
        });
    }
    private setDisable(disable: boolean): void {
        this.useDisable = disable;
        this.showLoading = disable;
    }
    private getChartYMax(): number {
        return this.yMax === -1 ? null : this.yMax;
    }
    private loadLoadChartData(from?: number, to?: number): void {
        const target = this.getTargetInfo();
        if (this.selectedAgent === '') {
            this.passDownChartData(this.agentHistogramDataService.makeChartDataForLoad(target.timeSeriesHistogram, this.timezone, [this.dateFormatMonth, this.dateFormatDay], this.getChartYMax()));
        } else {
            this.passDownChartData(this.agentHistogramDataService.makeChartDataForLoad(target['agentTimeSeriesHistogram'][this.selectedAgent], this.timezone, [this.dateFormatMonth, this.dateFormatDay], this.getChartYMax()));
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
            return this.serverMapData.getLinkData(this.selectedTarget.link[0]);
        }
    }
    onNotifyMax(max: number) {
        if (max > this.yMax) {
            this.yMax = max;
            this.storeHelperService.dispatch(new Actions.ChangeLoadChartYMax(max));
        }
    }
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
