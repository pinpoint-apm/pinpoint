import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

import { Period } from 'app/core/models/period';
import { UrlPath } from 'app/shared/models';
import { WebAppSettingDataService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';

@Component({
    selector: 'pp-search-period-container',
    templateUrl: './search-period-container.component.html',
    styleUrls: ['./search-period-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SearchPeriodContainerComponent implements OnInit {
    periodList: Period[];
    userDefaultPeriod: Period;

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        this.periodList = this.webAppSettingDataService.getPeriodList(UrlPath.MAIN);
        this.userDefaultPeriod = this.webAppSettingDataService.getUserDefaultPeriod();
    }

    onChangeUserDefaultPeriod(value: Period): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SET_SEARCH_PERIOD_IN_CONFIGURATION, value.getValueWithTime());
        this.webAppSettingDataService.setUserDefaultPeriod(value);
    }
}
