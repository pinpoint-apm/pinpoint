import { Component, OnInit, OnDestroy, Input, ComponentFactoryResolver, Injector, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { WebAppSettingDataService, AnalyticsService, TRACKED_EVENT_LIST, DynamicPopupService } from 'app/shared/services';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';
import { LoadChartChangeNotificationService, ILoadChartNotificationData, SOURCE_TYPE } from './load-chart-change-notification.service';

@Component({
    selector: 'pp-load-chart-container',
    templateUrl: './load-chart-container.component.html',
    styleUrls: ['./load-chart-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoadChartContainerComponent implements OnInit, OnDestroy {
    @Input() sourceType: string;
    private unsubscribe: Subject<null> = new Subject();
    hiddenChart = false;
    chartData: IHistogram[];
    chartColors: string[];
    i18nText = {
        NO_DATA: '',
        FAILED_TO_FETCH_DATA: ''
    };
    useDisable = false;
    showLoading = false;
    hasRequestError = false;
    hiddenComponent = false;
    constructor(
        private injector: Injector,
        private changeDetectorRef: ChangeDetectorRef,
        private translateService: TranslateService,
        private webAppSettingDataService: WebAppSettingDataService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private loadChartChangeNotificationService: LoadChartChangeNotificationService
    ) {}
    ngOnInit() {
        this.chartColors = this.webAppSettingDataService.getColorByRequest();
        this.translateService.get([
            'COMMON.NO_DATA',
            'COMMON.FAILED_TO_FETCH_DATA'
        ]).subscribe((text: {[key: string]: string}) => {
            this.i18nText = {
                NO_DATA: text['COMMON.NO_DATA'],
                FAILED_TO_FETCH_DATA: text['COMMON.FAILED_TO_FETCH_DATA']
            };
        });
        this.loadChartChangeNotificationService.getObservable(this.sourceType).pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((notificationData: ILoadChartNotificationData) => {
            if (notificationData.hidden) {
                this.hiddenComponent = true;
                this.changeDetectorRef.detectChanges();
            } else {
                this.hiddenComponent = false;
                this.passDownChartData(notificationData.chart);
            }
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private setDisable(disable: boolean): void {
        this.useDisable = disable;
        this.showLoading = disable;
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
        this.changeDetectorRef.detectChanges();
    }
    getChartMessage(): string {
        return this.hasRequestError ? this.i18nText.FAILED_TO_FETCH_DATA : this.i18nText.NO_DATA;
    }
    onNotifyMax(max: number) {
        if (this.sourceType !== SOURCE_TYPE.INFO_PER_SERVER) {
            this.loadChartChangeNotificationService.setYMax(max);
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
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }
}
