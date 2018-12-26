import { Component, ChangeDetectionStrategy, ChangeDetectorRef, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { combineLatest, Subject } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import {
    WebAppSettingDataService,
    NewUrlStateNotificationService,
    UrlRouteManagerService,
    StoreHelperService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService
} from 'app/shared/services';
import { Actions } from 'app/shared/store';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { ScatterChart } from './class/scatter-chart.class';
import { ScatterChartInteractionService } from './scatter-chart-interaction.service';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';

@Component({
    selector: 'pp-scatter-chart-for-filtered-map-info-per-server-container',
    templateUrl: './scatter-chart-for-filtered-map-info-per-server-container.component.html',
    styleUrls: ['./scatter-chart-for-filtered-map-info-per-server-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ScatterChartForFilteredMapInfoPerServerContainerComponent implements OnInit, AfterViewInit, OnDestroy {
    instanceKey = 'filtered-map-info-per-server';
    addWindow = false;
    i18nText: { [key: string]: string };
    isChangedTarget: boolean;
    selectedTarget: ISelectedTarget;
    selectedApplication: string;
    unsubscribe: Subject<null> = new Subject();
    hideSettingPopup = true;
    selectedAgent: string;
    typeInfo = [{
        name: 'failed',
        color: '#E95459',
        order: 10
    }, {
        name: 'success',
        color: '#34B994',
        order: 20
    }];
    typeCount: object;
    width = 460;
    height = 230;
    min: number;
    max: number;
    fromX: number;
    toX: number;
    fromY: number;
    toY: number;
    application: string;
    scatterChartMode: string;
    scatterChartDataOfAllNode: any[] = [];
    timezone: string;
    dateFormat: string[];
    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private storeHelperService: StoreHelperService,
        private translateService: TranslateService,
        private webAppSettingDataService: WebAppSettingDataService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private scatterChartInteractionService: ScatterChartInteractionService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService
    ) {}
    ngOnInit() {
        this.setScatterY();
        combineLatest(
            this.translateService.get('COMMON.NO_DATA'),
            this.translateService.get('COMMON.FAILED_TO_FETCH_DATA')
        ).subscribe((i18n: string[]) => {
            this.i18nText = {
                NO_DATA: i18n[0],
                FAILED_TO_FETCH_DATA: i18n[1]
            };
        });
        this.connectStore();
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter((urlService: NewUrlStateNotificationService) => {
                return urlService.hasValue(UrlPathId.APPLICATION);
            })
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            this.scatterChartMode = urlService.isRealTimeMode() ? ScatterChart.MODE.REALTIME : ScatterChart.MODE.STATIC;
            this.application = urlService.getPathValue(UrlPathId.APPLICATION).getKeyStr();
            this.selectedAgent = '';
            this.fromX = urlService.getStartTimeToNumber();
            this.toX = urlService.getEndTimeToNumber();
            this.changeDetectorRef.detectChanges();
        });
    }
    ngAfterViewInit() {
        this.storeHelperService.getInfoPerServerState(this.unsubscribe).subscribe((visibleState: boolean) => {
            if (visibleState && this.isChangedTarget) {
                this.scatterChartDataOfAllNode.forEach((scatterData: any) => {
                    this.scatterChartInteractionService.addChartData(this.instanceKey, scatterData[this.selectedApplication]);
                });
                this.isChangedTarget = false;
            }
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private setScatterY() {
        const scatterYData = this.webAppSettingDataService.getScatterY(this.instanceKey);
        this.fromY = scatterYData.min;
        this.toY = scatterYData.max;
    }
    private connectStore(): void {
        this.storeHelperService.getTimezone(this.unsubscribe).subscribe((timezone: string) => {
            this.timezone = timezone;
            this.changeDetectorRef.detectChanges();
        });
        this.storeHelperService.getDateFormatArray(this.unsubscribe, 3, 4).subscribe((format: string[]) => {
            this.dateFormat = format;
            this.changeDetectorRef.detectChanges();
        });
        this.storeHelperService.getAgentSelectionForServerList(this.unsubscribe).pipe(
            filter((chartData: IAgentSelection) => {
                return (chartData && chartData.agent) ? true : false;
            })
        ).subscribe((chartData: IAgentSelection) => {
            this.selectedAgent = chartData.agent;
            this.changeDetectorRef.detectChanges();
            this.scatterChartInteractionService.changeAgent(this.instanceKey, chartData.agent);
        });
        this.storeHelperService.getScatterChartData(this.unsubscribe).subscribe((scatterChartData: IScatterData[]) => {
            this.scatterChartDataOfAllNode = scatterChartData;
        });
        this.storeHelperService.getServerMapTargetSelected(this.unsubscribe).pipe(
            filter((target: ISelectedTarget) => {
                return target && (target.isNode === true || target.isNode === false) ? true : false;
            })
        ).subscribe((target: ISelectedTarget) => {
            this.isChangedTarget = true;
            this.selectedTarget = target;
            this.selectedAgent = '';
            this.selectedApplication = this.selectedTarget.node[0];
            this.scatterChartInteractionService.reset(this.instanceKey, this.selectedApplication, this.selectedAgent, this.fromX, this.toX, this.scatterChartMode);
            this.changeDetectorRef.detectChanges();
        });
    }
    isHide(): boolean {
        return false;
    }
    checkClass(): string {
        return this.hideSettingPopup ? '' : 'l-popup-on';
    }
    isHideSettingPopup(): boolean {
        return this.hideSettingPopup;
    }
    onApplySetting(params: any): void {
        this.fromY = params.min;
        this.toY = params.max;
        this.scatterChartInteractionService.changeYRange({
            instanceKey: this.instanceKey,
            from: params.min,
            to: params.max
        });
        this.hideSettingPopup = true;
        this.webAppSettingDataService.setScatterY(this.instanceKey, { min: params.min, max: params.max });
    }
    onCancelSetting(): void {
        this.hideSettingPopup = true;
    }
    onShowSetting(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_SCATTER_SETTING);
        this.hideSettingPopup = false;
    }
    onDownload(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.DOWNLOAD_SCATTER);
        this.scatterChartInteractionService.downloadChart(this.instanceKey);
    }
    onOpenScatterPage(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.GO_TO_FULL_SCREEN_SCATTER);
        this.urlRouteManagerService.openPage([
            UrlPath.SCATTER_FULL_SCREEN_MODE,
            this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
            this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
            this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime(),
            this.selectedAgent
        ]);
    }
    onShowHelp($event: MouseEvent): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.TOGGLE_HELP_VIEWER, HELP_VIEWER_LIST.SCATTER);
        const {left, top, width, height} = ($event.target as HTMLElement).getBoundingClientRect();

        this.dynamicPopupService.openPopup({
            data: HELP_VIEWER_LIST.SCATTER,
            coord: {
                coordX: left + width / 2,
                coordY: top + height / 2
            },
            component: HelpViewerPopupContainerComponent
        });
    }
    onChangedSelectType(params: {instanceKey: string, name: string, checked: boolean}): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CHANGE_SCATTER_CHART_STATE, `${params.name}: ${params.checked ? `on` : `off`}`);
        this.scatterChartInteractionService.changeViewType(params);
    }
    onChangeTransactionCount(params: object): void {
        this.typeCount = params;
    }
    onChangeRangeX(params: IScatterXRange): void {
        this.storeHelperService.dispatch(new Actions.UpdateRealTimeScatterChartXRange(params));
    }
    onSelectArea(params: any): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_TRANSACTION_LIST);
        this.urlRouteManagerService.openPage([
            UrlPath.TRANSACTION_LIST,
            this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
            this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
            this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime()
        ], `${this.selectedApplication}|${params.x.from}|${params.x.to}|${params.y.from}|${params.y.to}|${this.selectedAgent}|${params.type.join(',')}`);
    }
}
