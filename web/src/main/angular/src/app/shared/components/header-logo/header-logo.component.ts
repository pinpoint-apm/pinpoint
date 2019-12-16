import { Component, OnInit } from '@angular/core';

import { WebAppSettingDataService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';

@Component({
    selector: 'pp-header-logo',
    templateUrl: './header-logo.component.html',
    styleUrls: ['./header-logo.component.css']
})
export class HeaderLogoComponent implements OnInit {
    logoPath: string;
    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private analyticsService: AnalyticsService,
    ) { }

    ngOnInit() {
        this.logoPath = this.webAppSettingDataService.getLogoPath();
    }

    onLogoClick(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_LOGO_BUTTON);
    }
}
