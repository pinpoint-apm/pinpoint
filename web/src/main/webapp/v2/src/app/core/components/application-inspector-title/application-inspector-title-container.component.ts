import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

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
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ApplicationInspectorTitleContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    agentId: string;
    funcImagePath: Function;
    applicationServiceType: string;
    applicationName: string;
    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private webAppSettingDataService: WebAppSettingDataService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        this.funcImagePath = this.webAppSettingDataService.getIconPathMakeFunc();
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            if (urlService.hasValue(UrlPathId.APPLICATION)) {
                this.applicationName = urlService.getPathValue(UrlPathId.APPLICATION).getApplicationName();
                this.applicationServiceType = urlService.getPathValue(UrlPathId.APPLICATION).getServiceType();
            }
            if (urlService.hasValue(UrlPathId.AGENT_ID)) {
                this.agentId = urlService.getPathValue(UrlPathId.AGENT_ID);
            } else {
                this.agentId = '';
            }
            this.changeDetectorRef.detectChanges();
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    isEmptyAgentId(): boolean {
        return this.agentId === '';
    }
    getApplicationIcon(): string {
        return this.funcImagePath(this.applicationServiceType);
    }
    onSelectApplication() {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.GO_TO_APPLICATION_INSPECTOR);
        this.urlRouteManagerService.moveOnPage({
            url: [
                UrlPath.INSPECTOR,
                this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime()
            ]
        });
    }
}
