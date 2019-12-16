import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';

import { UrlPath, UrlPathId } from 'app/shared/models';
import {
    UrlRouteManagerService,
    WebAppSettingDataService,
    NewUrlStateNotificationService,
    AnalyticsService,
    TRACKED_EVENT_LIST
} from 'app/shared/services';

@Component({
    selector: 'pp-application-inspector-title-container',
    templateUrl: './application-inspector-title-container.component.html',
    styleUrls: ['./application-inspector-title-container.component.css'],
})
export class ApplicationInspectorTitleContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    agentId: string;
    funcImagePath: Function;
    applicationServiceType: string;
    applicationName: string;
    applicationIconImagePath: string;

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        this.funcImagePath = this.webAppSettingDataService.getIconPathMakeFunc();
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter((urlService: NewUrlStateNotificationService) => urlService.hasValue(UrlPathId.APPLICATION))
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            this.applicationName = urlService.getPathValue(UrlPathId.APPLICATION).getApplicationName();
            this.applicationServiceType = urlService.getPathValue(UrlPathId.APPLICATION).getServiceType();
            this.applicationIconImagePath = this.funcImagePath(this.applicationServiceType);
            this.agentId = urlService.hasValue(UrlPathId.AGENT_ID) ? urlService.getPathValue(UrlPathId.AGENT_ID) : '';
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    onSelectApplication() {
        const url = this.newUrlStateNotificationService.isRealTimeMode() ?
            [
                UrlPath.INSPECTOR,
                UrlPath.REAL_TIME,
                this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
            ] :
            [
                UrlPath.INSPECTOR,
                this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime(),
            ];

        this.urlRouteManagerService.moveOnPage({ url });
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.GO_TO_APPLICATION_INSPECTOR);
    }
}
