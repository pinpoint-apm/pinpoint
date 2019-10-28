import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { takeUntil, tap } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import {
    TranslateReplaceService,
    StoreHelperService,
    WebAppSettingDataService,
    UrlRouteManagerService,
    NewUrlStateNotificationService,
    AnalyticsService,
    TRACKED_EVENT_LIST
} from 'app/shared/services';
import { UrlPathId } from 'app/shared/models';
import { Period } from 'app/core/models/period';
import { EndTime } from 'app/core/models/end-time';

@Component({
    selector: 'pp-period-selector-container',
    templateUrl: './period-selector-container.component.html',
    styleUrls: ['./period-selector-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class PeriodSelectorContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    i18nText = {
        MAX_PERIOD: '',
    };
    hiddenComponent: boolean;
    selectedPeriod: Period;
    selectedEndTime: EndTime;
    periodList: Array<Period>;
    maxPeriod: number;
    isRealTimeMode: boolean;
    showRealTimeButton: boolean;
    timezone$: Observable<string>;
    dateFormat$: Observable<string>;

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private storeHelperService: StoreHelperService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private analyticsService: AnalyticsService,
        private cd: ChangeDetectorRef,
    ) {}

    ngOnInit() {
        this.periodList = this.webAppSettingDataService.getPeriodList(this.newUrlStateNotificationService.getStartPath());
        this.maxPeriod = this.periodList[this.periodList.length - 1].getValue();
        this.getI18NText();
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            tap((urlService: NewUrlStateNotificationService) => {
                this.showRealTimeButton = urlService.showRealTimeButton();
                this.isRealTimeMode = urlService.isRealTimeMode();
            })
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            if (this.showRealTimeButton && this.isRealTimeMode) {
                this.hiddenComponent = false;
                this.selectedPeriod = this.webAppSettingDataService.getSystemDefaultPeriod();
                this.selectedEndTime = EndTime.newByNumber(urlService.getUrlServerTimeData());
            } else {
                if (urlService.hasValue(UrlPathId.PERIOD, UrlPathId.END_TIME)) {
                    this.hiddenComponent = false;
                    this.selectedPeriod = urlService.getPathValue(UrlPathId.PERIOD);
                    this.selectedEndTime = urlService.getPathValue(UrlPathId.END_TIME);
                } else {
                    this.hiddenComponent = true;
                }
            }

            this.cd.markForCheck();
        });
        this.connectStore();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private connectStore(): void {
        this.timezone$ = this.storeHelperService.getTimezone(this.unsubscribe);
        this.dateFormat$ = this.storeHelperService.getDateFormat(this.unsubscribe, 1);
    }

    private getI18NText(): void {
        this.translateService.get('COMMON.MAX_SEARCH_PERIOD').subscribe((i18n: string) => {
            this.i18nText.MAX_PERIOD = this.translateReplaceService.replace(i18n, this.maxPeriod / 24 / 60);
        });
    }

    onChangePeriodTime(selectedPeriod: string): void {
        if (this.newUrlStateNotificationService.isRealTimeMode(selectedPeriod)) {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SET_PERIOD_AS_REAL_TIME);
            this.urlRouteManagerService.moveToRealTime();
        } else {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SELECT_PERIOD, selectedPeriod);
            this.urlRouteManagerService.move({
                url: [
                    this.newUrlStateNotificationService.getStartPath(),
                    this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                    selectedPeriod
                ],
                needServerTimeRequest: true,
                nextUrl: this.newUrlStateNotificationService.hasValue(UrlPathId.AGENT_ID) ? [this.newUrlStateNotificationService.getPathValue(UrlPathId.AGENT_ID)] : []
            });
        }
    }

    onChangeCalendarTime(oChangeTime: any): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SELECT_PERIOD, oChangeTime.period.getValueWithTime());
        this.urlRouteManagerService.move({
            url: [
                this.newUrlStateNotificationService.getStartPath(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                oChangeTime.period.getValueWithTime(),
                oChangeTime.endTime.getEndTime()
            ],
            needServerTimeRequest: false,
            nextUrl: this.newUrlStateNotificationService.hasValue(UrlPathId.AGENT_ID) ? [this.newUrlStateNotificationService.getPathValue(UrlPathId.AGENT_ID)] : []
        });
    }
}
