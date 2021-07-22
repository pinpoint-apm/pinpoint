import { Component, OnInit, AfterViewInit, OnDestroy, ComponentFactoryResolver, Injector, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { forkJoin, Subject } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';
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
import { UrlPath, UrlPathId, UrlQuery } from 'app/shared/models';
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
    private unsubscribe = new Subject<void>();
    private _hideSettingPopup = true;

    wrapperClassName = '';
    instanceKey = 'filtered-map-info-per-server';
    addWindow = false;
    i18nText: { [key: string]: string };
    isChangedTarget: boolean;
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
    scatterChartDataOfAllNode: any[] = [];
    timezone: string;
    dateFormat: string[];
    showBlockMessagePopup = false;
    enableServerSideScan: boolean;

    constructor(
        private storeHelperService: StoreHelperService,
        private translateService: TranslateService,
        private webAppSettingDataService: WebAppSettingDataService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private scatterChartInteractionService: ScatterChartInteractionService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
        private messageQueueService: MessageQueueService,
        private cd: ChangeDetectorRef,
    ) {}

    ngOnInit() {
        this.enableServerSideScan = this.webAppSettingDataService.getExperimentalOption('scatterScan');
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
        this.connectStore();
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter((urlService: NewUrlStateNotificationService) => urlService.hasValue(UrlPathId.APPLICATION))
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            this.scatterChartMode = urlService.isRealTimeMode() ? ScatterChart.MODE.REALTIME : ScatterChart.MODE.STATIC;
            this.application = urlService.getPathValue(UrlPathId.APPLICATION).getKeyStr();
            this.selectedAgent = '';
            this.fromX = urlService.getStartTimeToNumber();
            this.toX = urlService.getEndTimeToNumber();
            this.cd.detectChanges();
        });
    }

    ngAfterViewInit() {
        this.storeHelperService.getInfoPerServerState(this.unsubscribe).pipe(
            filter((visibleState: boolean) => visibleState && this.isChangedTarget)
        ).subscribe((visibleState: boolean) => {
            this.scatterChartDataOfAllNode.forEach((scatterData: any) => {
                this.scatterChartInteractionService.addChartData(this.instanceKey, scatterData[this.selectedApplication]);
            });
            this.isChangedTarget = false;
            this.cd.detectChanges();
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private reset(range?: {[key: string]: number}): void {
        this.toX = range ? range.toX : Date.now();
        this.fromX = range ? range.fromX : this.toX - this.webAppSettingDataService.getSystemDefaultPeriod().getMiliSeconds();

        this.scatterChartInteractionService.reset(this.instanceKey, this.selectedApplication, this.selectedAgent, this.fromX, this.toX, this.scatterChartMode);
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
        this.storeHelperService.getAgentSelectionForServerList(this.unsubscribe).pipe(
            filter((data: IAgentSelection) => !!data),
        ).subscribe(({agent}: IAgentSelection) => {
            this.selectedAgent = agent;
            this.scatterChartInteractionService.changeAgent(this.instanceKey, agent);
            this.cd.detectChanges();
        });
        this.storeHelperService.getScatterChartData(this.unsubscribe).subscribe((scatterChartData: IScatterData[]) => {
            this.scatterChartDataOfAllNode = scatterChartData;
            this.cd.detectChanges();
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_TARGET_SELECT).subscribe((target: ISelectedTarget) => {
            this.isChangedTarget = true;
            this.selectedTarget = target;
            this.selectedAgent = '';
            this.selectedApplication = this.selectedTarget.node[0];
            this.reset({fromX: this.fromX, toX: this.toX});
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
        this.reset({fromX: this.fromX, toX: this.toX});
        this.scatterChartDataOfAllNode.forEach((scatterData: any) => {
            this.scatterChartInteractionService.addChartData(this.instanceKey, scatterData[this.selectedApplication]);
        });
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
        this.urlRouteManagerService.openPage({
            path: [
                UrlPath.SCATTER_FULL_SCREEN_MODE,
                `${this.selectedApplication.replace('^', '@')}`,
                this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime(),
                this.selectedAgent
            ]
        });
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

    onChangeRangeX(params: IScatterXRange): void {
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.REAL_TIME_SCATTER_CHART_X_RANGE,
            param: params
        });
    }

    onSelectArea(params: any): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SELECT_AREA_ON_SCATTER);
        const returnOpenWindow = this.urlRouteManagerService.openPage({
            path: [
                UrlPath.TRANSACTION_LIST,
                `${this.selectedApplication.replace('^', '@')}`,
                this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime()
            ],
            queryParams: {
                [UrlQuery.DRAG_INFO]: {
                    x1: params.x.from,
                    x2: params.x.to,
                    y1: params.y.from,
                    y2: params.y.to,
                    agentId: this.selectedAgent,
                    dotStatus: params.type
                }
            },
        });

        if (returnOpenWindow === null || returnOpenWindow === undefined) {
            this.showBlockMessagePopup = true;
        }
    }

    onCloseBlockMessage(): void {
        this.showBlockMessagePopup = false;
    }
}
