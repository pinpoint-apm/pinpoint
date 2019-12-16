import { Component, OnInit, OnDestroy, ChangeDetectionStrategy } from '@angular/core';
import { Observable, Subject } from 'rxjs';

import { Actions } from 'app/shared/store';
import { StoreHelperService, WebAppSettingDataService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';

@Component({
    selector: 'pp-date-format-container',
    templateUrl: './date-format-container.component.html',
    styleUrls: ['./date-format-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DateFormatContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    timezone$: Observable<string>;
    dateFormatList: string[][];
    currentDateFormatIndex$: Observable<number>;

    constructor(
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        this.dateFormatList = this.webAppSettingDataService.getDateFormatList();
        this.connectStore();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private connectStore(): void {
        this.timezone$ = this.storeHelperService.getTimezone(this.unsubscribe);
        this.currentDateFormatIndex$ = this.storeHelperService.getDateFormatIndex(this.unsubscribe);
    }

    onChangeDateFormat(dateFormatIndex: number): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SET_DATE_FORMAT_IN_CONFIGURATION, this.dateFormatList[dateFormatIndex][0]);
        this.webAppSettingDataService.setDateFormat(dateFormatIndex);
        this.storeHelperService.dispatch(new Actions.ChangeDateFormat(dateFormatIndex));
    }
}
