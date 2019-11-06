import { Component, OnInit } from '@angular/core';

import { AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';

@Component({
    selector: 'pp-configuration-help-container',
    templateUrl: 'configuration-help-container.component.html',
    styleUrls: ['./configuration-help-container.component.css'],
})
export class ConfigurationHelpContainerComponent implements OnInit {
    constructor(
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {}
    onLinkClick(type: string): void {
        type === 'FAQ' ? this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_FAQ_BUTTON) :
        type === 'Issues' ? this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_ISSUES_BUTTON) :
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_ISSUES_BUTTON);
    }

    onStartGuideClick(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_START_GUIDE);
    }

    onTechnicalOverviewClick(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_TECHNICAL_OVERVIEW);
    }
}
