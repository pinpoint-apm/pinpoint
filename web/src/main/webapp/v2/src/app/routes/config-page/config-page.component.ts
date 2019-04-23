import { Component, OnInit } from '@angular/core';

import { UrlRouteManagerService, NewUrlStateNotificationService, TRACKED_EVENT_LIST, AnalyticsService } from 'app/shared/services';
import { UrlPath } from 'app/shared/models';

@Component({
    selector: 'pp-config-page',
    templateUrl: './config-page.component.html',
    styleUrls: ['./config-page.component.css']
})
export class ConfigPageComponent implements OnInit {
    constructor(
        private urlRouteManagerService: UrlRouteManagerService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {}
    onClickExit(): void {
        const { startPath, pathParams, queryParams } = this.newUrlStateNotificationService.getPrevPageUrlInfo();
        const url = startPath === UrlPath.CONFIG ? [UrlPath.MAIN] : [startPath, ...[ ...pathParams.values() ]];
        const queryParam = [ ...queryParams.entries() ].reduce((acc: object, [key, value]: string[]) => {
            return { ...acc, [key]: value };
        }, {});

        this.urlRouteManagerService.moveOnPage({ url, queryParam });
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_CONFIGURATION_PAGE_EXIT_BUTTON);
    }

    onMenuClick(menu: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_CONFIGURATION_MENU, menu);
    }
}
