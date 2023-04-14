import { Component, OnDestroy, OnInit } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { takeUntil, map, tap } from 'rxjs/operators';

import { NewUrlStateNotificationService, UrlRouteManagerService, WebAppSettingDataService } from 'app/shared/services';
import { UrlPath, UrlPathId } from 'app/shared/models';

@Component({
    selector: 'pp-url-statistic-page',
    templateUrl: './url-statistic-page.component.html',
    styleUrls: ['./url-statistic-page.component.css']
})
export class UrlStatisticPageComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private funcImagePath: Function;

    showUrlStat$: Observable<boolean>;
    showSideMenu: boolean;
    mainSectionStyle = {};
    isAppActivated: boolean;
    selectedAppImg: string;
    selectedAppName: string;

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private webAppSettingDataService: WebAppSettingDataService,
        private urlRouteManagerService: UrlRouteManagerService,
    ) {}

    ngOnInit() {
        this.showUrlStat$ = this.webAppSettingDataService.showUrlStat().pipe(
            tap((showUrlStat) => {
                if (!showUrlStat) {
                    this.urlRouteManagerService.moveToMain();
                }
            })
        );

        this.funcImagePath = this.webAppSettingDataService.getIconPathMakeFunc();
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            tap((urlService: NewUrlStateNotificationService) => {
                if (urlService.hasValue(UrlPathId.APPLICATION)) {
                    const selectedApp: IApplication = urlService.getPathValue(UrlPathId.APPLICATION);

                    this.selectedAppName = selectedApp.getApplicationName();
                    this.selectedAppImg = this.funcImagePath(selectedApp.getServiceType());
                    this.isAppActivated = !urlService.hasValue(UrlPathId.AGENT_ID);
                }
            }),
            map((urlService: NewUrlStateNotificationService) => {
                return urlService.isRealTimeMode() || urlService.hasValue(UrlPathId.END_TIME);
            })
        ).subscribe((showSideMenu: boolean) => {
            this.showSideMenu = showSideMenu;
            this.mainSectionStyle = {
                width: showSideMenu ? 'calc(100% - 250px)' : '100%'
            };
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    onSelectApp(): void {
        this.urlRouteManagerService.moveOnPage({
            url: [
                UrlPath.URL_STATISTIC,
                this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime(),
            ]
        })
    }
}
