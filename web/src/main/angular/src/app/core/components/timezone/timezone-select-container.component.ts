import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import * as moment from 'moment-timezone';
import { Observable, Subject } from 'rxjs';

import { Actions } from 'app/shared/store';
import { StoreHelperService, WebAppSettingDataService, AnalyticsService, TRACKED_EVENT_LIST  } from 'app/shared/services';

@Component({
    selector: 'pp-timezone-select-container',
    templateUrl: './timezone-select-container.component.html',
    styleUrls: ['./timezone-select-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TimezoneSelectContainerComponent implements OnInit {
    private unsubscribe = new Subject<void>();

    timezoneList: string[];
    currentTimezone$: Observable<string>;

    constructor(
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        this.initTimezoneList();
        this.initCurrentTimezone();
    }

    private initTimezoneList(): void {
        this.timezoneList = moment.tz.names();
    }

    private initCurrentTimezone(): void {
        this.currentTimezone$ = this.storeHelperService.getTimezone(this.unsubscribe);
    }

    onChangeTimezone(timezone: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SET_TIMEZONE_IN_CONFIGURATION, timezone);
        this.webAppSettingDataService.setTimezone(timezone);
        this.storeHelperService.dispatch(new Actions.ChangeTimezone(timezone));
    }
}
