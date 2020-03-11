import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { of, Subject, forkJoin, fromEvent } from 'rxjs';
import { takeUntil, filter, delay } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import {
    WebAppSettingDataService,
    NewUrlStateNotificationService,
    UrlRouteManagerService,
    StoreHelperService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService,
    MessageQueueService,
    MESSAGE_TO
} from 'app/shared/services';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { EndTime } from 'app/core/models';
import { ScatterChartDataService } from './scatter-chart-data.service';
import { ScatterChart } from './class/scatter-chart.class';
import { ScatterChartInteractionService } from './scatter-chart-interaction.service';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';

@Component({
    selector: 'pp-scatter-chart-container',
    templateUrl: './scatter-chart-container.component.html',
    styleUrls: ['./scatter-chart-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ScatterChartContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private _hideSettingPopup = true;

    wrapperClassName = '';
    instanceKey = 'side-bar';
    addWindow = true;
    i18nText: { [key: string]: string };
    currentRange: { from: number, to: number } = {
        from : 0,
        to: 0
    };
    selectedTarget: ISelectedTarget;
    selectedApplication: string;
    selectedAgent: string;
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
    timezone: string;
    dateFormat: string[];
    showBlockMessagePopup = false;
    shouldHide = true;

    constructor(
        private storeHelperService: StoreHelperService,
        private translateService: TranslateService,
        private webAppSettingDataService: WebAppSettingDataService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private scatterChartDataService: ScatterChartDataService,
        private scatterChartInteractionService: ScatterChartInteractionService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
        private messageQueueService: MessageQueueService,
        private cd: ChangeDetectorRef,
    ) {}

    ngOnInit() {
        this.setScatterY();

        forkJoin(
            this.translateService.get('COMMON.NO_DATA'),
            this.translateService.get('COMMON.FAILED_TO_FETCH_DATA'),
            this.translateService.get('COMMON.POPUP_BLOCK_MESSAGE')
        ).subscribe((i18n: string[]) => {
            this.i18nText = {
                NO_DATA: i18n[0],
                FAILED_TO_FETCH_DATA: i18n[1],
                POPUP_BLOCK_MESSAGE: i18n[2]
            };
        });

        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter((urlService: NewUrlStateNotificationService) => urlService.hasValue(UrlPathId.APPLICATION))
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            this.scatterChartDataService.stopLoad();
            this.scatterChartMode = urlService.isRealTimeMode() ? ScatterChart.MODE.REALTIME : ScatterChart.MODE.STATIC;
            this.application = urlService.getPathValue(UrlPathId.APPLICATION).getKeyStr();
            this.selectedAgent = '';
            this.currentRange.from = this.fromX = urlService.getStartTimeToNumber();
            this.currentRange.to = this.toX = urlService.getEndTimeToNumber();
            this.cd.detectChanges();
        });

        this.scatterChartDataService.outScatterData$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((scatterData: IScatterData) => {
            if (!scatterData.complete) {
                this.scatterChartDataService.loadData(
                    this.selectedApplication.split('^')[0],
                    this.fromX,
                    scatterData.resultFrom - 1,
                    this.getGroupUnitX(),
                    this.getGroupUnitY(),
                    false
                );
            }
            this.scatterChartInteractionService.addChartData(this.instanceKey, scatterData);
            this.cd.detectChanges();
        });

        this.scatterChartDataService.outRealTimeScatterData$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((scatterData: IScatterData) => {
            if (scatterData.reset) {
                this.fromX = scatterData.currentServerTime - this.webAppSettingDataService.getSystemDefaultPeriod().getMiliSeconds();
                this.toX = scatterData.currentServerTime;
                this.scatterChartInteractionService.reset(this.instanceKey, this.selectedApplication, this.selectedAgent, this.fromX, this.toX, this.scatterChartMode);
                of(1).pipe(delay(1000)).subscribe((useless: number) => {
                    this.getScatterData();
                });
            } else {
                this.scatterChartInteractionService.addChartData(this.instanceKey, scatterData);
            }
            this.cd.detectChanges();
        });

        this.scatterChartDataService.outScatterErrorData$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((error: IServerErrorFormat) => {
            this.scatterChartInteractionService.setError(error);
            this.cd.detectChanges();
        });
        this.scatterChartDataService.outRealTimeScatterErrorData$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((error: IServerErrorFormat) => {
        });

        this.connectStore();
        this.addEventListener();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private addEventListener(): void {
        const visibility$ = fromEvent(document, 'visibilitychange').pipe(
            takeUntil(this.unsubscribe),
            filter(() => this.scatterChartMode === ScatterChart.MODE.REALTIME)
        );

        // visible
        visibility$.pipe(
            filter(() => !document.hidden),
            filter(() => !this.scatterChartDataService.isConnected())
        ).subscribe(() => {
            this.getScatterData();
        });

        // hidden
        visibility$.pipe(
            filter(() => document.hidden),
            delay(10000),
            filter(() => document.hidden),
        ).subscribe(() => {
            this.scatterChartDataService.stopLoad();
        });
    }

    private setScatterY() {
        const {min, max} = this.webAppSettingDataService.getScatterY(this.instanceKey);

        this.fromY = min;
        this.toY = max;
    }

    private connectStore(): void {
        this.storeHelperService.getTimezone(this.unsubscribe).subscribe((timezone: string) => {
            this.timezone = timezone;
            this.cd.detectChanges();
        });
        this.storeHelperService.getDateFormatArray(this.unsubscribe, 4, 5).subscribe((format: string[]) => {
            this.dateFormat = format;
            this.cd.detectChanges();
        });
        this.storeHelperService.getAgentSelection(this.unsubscribe).pipe(
            filter((agent: string) => this.selectedAgent !== agent)
        ).subscribe((agent: string) => {
            this.selectedAgent = agent;
            this.scatterChartInteractionService.changeAgent(this.instanceKey, agent);
            this.cd.detectChanges();
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_TARGET_SELECT).subscribe((target: ISelectedTarget) => {
            this.selectedTarget = target;
            this.shouldHide = !(this.selectedTarget.isNode && this.selectedTarget.isWAS && !this.selectedTarget.isMerged);
            if (!this.shouldHide) {
                this.selectedAgent = '';
                this.selectedApplication = this.selectedTarget.node[0];
                this.scatterChartInteractionService.reset(this.instanceKey, this.selectedApplication, this.selectedAgent, this.fromX, this.toX, this.scatterChartMode);
                this.getScatterData();
            }
            this.cd.detectChanges();
        });
    }

    set hideSettingPopup(hide: boolean) {
        this._hideSettingPopup = hide;
        this.wrapperClassName = hide ? '' : 'l-popup-on';
    }

    get hideSettingPopup(): boolean {
        return this._hideSettingPopup;
    }

    getScatterData(): void {
        this.scatterChartDataService.loadData(
            this.selectedApplication.split('^')[0],
            this.fromX,
            this.toX,
            this.getGroupUnitX(),
            this.getGroupUnitY()
        );
        if (this.scatterChartMode === ScatterChart.MODE.REALTIME) {
            this.scatterChartDataService.loadRealTimeDataV2(this.toX);
        }
    }

    getGroupUnitX(): number {
        return Math.round((this.toX - this.fromX) / this.width);
    }

    getGroupUnitY(): number {
        return Math.round((this.toY - this.fromY) / this.height);
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
        if (this.scatterChartMode === ScatterChart.MODE.STATIC) {
            this.urlRouteManagerService.openPage({
                path: [
                    UrlPath.SCATTER_FULL_SCREEN_MODE,
                    this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                    this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                    this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime(),
                    this.selectedAgent
                ]
            });
        } else {
            this.urlRouteManagerService.openPage({
                path: [
                    UrlPath.SCATTER_FULL_SCREEN_MODE,
                    UrlPathId.REAL_TIME,
                    this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                    this.selectedAgent
                ]
            });
        }
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
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }

    onChangedSelectType(params: {instanceKey: string, name: string, checked: boolean}): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CHANGE_SCATTER_CHART_STATE, `${params.name}: ${params.checked ? `on` : `off`}`);
        this.scatterChartInteractionService.changeViewType(params);
    }

    onChangeTransactionCount(params: object): void {
        this.typeCount = params;
    }

    onChangeRangeX(params: { from: number, to: number }): void {
        this.currentRange.from = params.from;
        this.currentRange.to = params.to;
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.REAL_TIME_SCATTER_CHART_X_RANGE,
            param: params
        });
    }

    onSelectArea(params: any): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_TRANSACTION_LIST);
        let returnOpenWindow;
        if (this.newUrlStateNotificationService.isRealTimeMode()) {
            returnOpenWindow = this.urlRouteManagerService.openPage({
                path: [
                    UrlPath.TRANSACTION_LIST,
                    this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                    this.webAppSettingDataService.getSystemDefaultPeriod().getValueWithTime(),
                    EndTime.newByNumber(this.currentRange.to).getEndTime(),
                ],
                metaInfo: `${this.selectedApplication}|${params.x.from}|${params.x.to}|${params.y.from}|${params.y.to}|${this.selectedAgent}|${params.type.join(',')}`
            });
        } else {
            returnOpenWindow = this.urlRouteManagerService.openPage({
                path: [
                    UrlPath.TRANSACTION_LIST,
                    this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                    this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                    this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime()
                ],
                metaInfo: `${this.selectedApplication}|${params.x.from}|${params.x.to}|${params.y.from}|${params.y.to}|${this.selectedAgent}|${params.type.join(',')}`
            });
        }
        if (returnOpenWindow === null || returnOpenWindow === undefined) {
            this.showBlockMessagePopup = true;
        }
    }

    onCloseBlockMessage(): void {
        this.showBlockMessagePopup = false;
    }
}
