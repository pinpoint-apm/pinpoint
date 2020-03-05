import { Component, OnInit, OnDestroy, ViewChild, ElementRef, Renderer2, ComponentFactoryResolver, Injector } from '@angular/core';
import { Observable, Subject, of, forkJoin, Subscription } from 'rxjs';
import { takeUntil, delay } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import {
    StoreHelperService,
    WebAppSettingDataService,
    NewUrlStateNotificationService,
    UrlRouteManagerService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService,
    MessageQueueService,
    MESSAGE_TO
} from 'app/shared/services';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { ScatterChartDataService } from './scatter-chart-data.service';
import { ScatterChart } from './class/scatter-chart.class';
import { ScatterChartInteractionService } from './scatter-chart-interaction.service';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';

@Component({
    selector: 'pp-scatter-chart-for-full-screen-mode-container',
    templateUrl: './scatter-chart-for-full-screen-mode-container.component.html',
    styleUrls: ['./scatter-chart-for-full-screen-mode-container.component.css']
})
export class ScatterChartForFullScreenModeContainerComponent implements OnInit, OnDestroy {
    @ViewChild('layerBackground', { static: true }) layerBackground: ElementRef;
    private unsubscribe = new Subject<void>();
    private _hideSettingPopup = true;

    instanceKey = 'full-screen-mode';
    addWindow = true;
    i18nText: { [key: string]: string };
    selectedTarget: ISelectedTarget;
    selectedApplication: string;
    scatterDataServiceSubscription: Subscription;
    selectedAgent: string;
    typeCount: object;
    width = 690;
    height = 345;
    min: number;
    max: number;
    fromX: number;
    toX: number;
    fromY: number;
    toY: number;
    application: string;
    scatterChartMode: string;
    timezone$: Observable<string>;
    dateFormat: string[];
    showBlockMessagePopup = false;

    constructor(
        private renderer: Renderer2,
        private translateService: TranslateService,
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private scatterChartDataService: ScatterChartDataService,
        private scatterChartInteractionService: ScatterChartInteractionService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
        private messageQueueService: MessageQueueService
    ) {}
    ngOnInit() {
        this.setScatterY();
        this.getI18NText();
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            this.scatterChartMode = urlService.isRealTimeMode() ? ScatterChart.MODE.REALTIME : ScatterChart.MODE.STATIC;
            this.selectedApplication = this.application = urlService.getPathValue(UrlPathId.APPLICATION).getKeyStr();
            this.selectedAgent = urlService.hasValue(UrlPathId.AGENT_ID) ? urlService.getPathValue(UrlPathId.AGENT_ID) : '';
            this.fromX = urlService.getStartTimeToNumber();
            this.toX = urlService.getEndTimeToNumber();
            of(1).pipe(delay(1)).subscribe(() => {
                this.scatterChartInteractionService.reset(this.instanceKey, this.selectedApplication, this.selectedAgent, this.fromX, this.toX, this.scatterChartMode);
                this.getScatterData();
            });
        });
        this.scatterChartDataService.outScatterData$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((scatterData: IScatterData) => {
            this.scatterChartInteractionService.addChartData(this.instanceKey, scatterData);
            if (scatterData.complete === false) {
                this.scatterChartDataService.loadData(
                    this.selectedApplication.split('^')[0],
                    this.fromX,
                    scatterData.resultFrom - 1,
                    this.getGroupUnitX(),
                    this.getGroupUnitY(),
                    false
                );
            }
        });
        this.scatterChartDataService.outRealTimeScatterData$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((scatterData: IScatterData) => {
            if (scatterData.reset === true) {
                this.fromX = scatterData.currentServerTime - this.webAppSettingDataService.getSystemDefaultPeriod().getMiliSeconds();
                this.toX = scatterData.currentServerTime;
                this.scatterChartInteractionService.reset(this.instanceKey, this.selectedApplication, this.selectedAgent, this.fromX, this.toX, this.scatterChartMode);
                of(1).pipe(delay(1000)).subscribe((useless: number) => {
                    this.getScatterData();
                });
            } else {
                this.scatterChartInteractionService.addChartData(this.instanceKey, scatterData);
            }
        });
        this.scatterChartDataService.outScatterErrorData$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((error: IServerErrorFormat) => {
            this.scatterChartInteractionService.setError(error);
        });
        this.scatterChartDataService.outRealTimeScatterErrorData$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((error: IServerErrorFormat) => {
        });
        this.connectStore();
    }

    ngOnDestroy() {
        if (this.scatterDataServiceSubscription) {
            this.scatterDataServiceSubscription.unsubscribe();
        }
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private setScatterY(): void {
        const {min, max} = this.webAppSettingDataService.getScatterY(this.instanceKey);

        this.fromY = min;
        this.toY = max;
    }

    private getI18NText(): void {
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
    }

    private connectStore(): void {
        this.timezone$ = this.storeHelperService.getTimezone(this.unsubscribe);
        this.storeHelperService.getDateFormatArray(this.unsubscribe, 4, 5).subscribe((dateFormat: string[]) => {
            this.dateFormat = dateFormat;
        });
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

    set hideSettingPopup(hide: boolean) {
        this._hideSettingPopup = hide;
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
        this.onHideSetting();
        this.webAppSettingDataService.setScatterY(this.instanceKey, { min: params.min, max: params.max });
    }

    onHideSetting(): void {
        this.renderer.setStyle(this.layerBackground.nativeElement, 'display', 'none');
        this.hideSettingPopup = true;
    }

    onShowSetting(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_SCATTER_SETTING);
        this.renderer.setStyle(this.layerBackground.nativeElement, 'display', 'block');
        this.hideSettingPopup = false;
    }

    onDownload(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.DOWNLOAD_SCATTER);
        this.scatterChartInteractionService.downloadChart(this.instanceKey);
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
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_TRANSACTION_LIST);
        const returnOpenWindow = this.urlRouteManagerService.openPage({
            path: [
                UrlPath.TRANSACTION_LIST,
                this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime()
            ],
            metaInfo: `${this.selectedApplication}|${params.x.from}|${params.x.to}|${params.y.from}|${params.y.to}|${this.selectedAgent}|${params.type.join(',')}`
        });

        if (returnOpenWindow === null || returnOpenWindow === undefined) {
            this.showBlockMessagePopup = true;
        }
    }

    onCloseBlockMessage(): void {
        this.showBlockMessagePopup = false;
    }
}
