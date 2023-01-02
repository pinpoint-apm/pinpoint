import { Component, OnDestroy, OnInit } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { map, tap, takeUntil } from 'rxjs/operators';

import { NewUrlStateNotificationService, UrlRouteManagerService, WebAppSettingDataService } from 'app/shared/services';
import { UrlPathId } from 'app/shared/models';

@Component({
    selector: 'pp-metric-page',
    templateUrl: './metric-page.component.html',
    styleUrls: ['./metric-page.component.css']
})

export class MetricPageComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    showSideMenu: boolean;

    showMetric$: Observable<boolean>;
    mainSectionStyle = {};

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private webAppSettingDataService: WebAppSettingDataService,
        private urlRouteManagerService: UrlRouteManagerService
    ) {}

    ngOnInit() {
        // * Take this approach because Angular triggers guard before resolver
        this.showMetric$ = this.webAppSettingDataService.showMetric().pipe(
            tap((showMetric) => {
                if (!showMetric) {
                    this.urlRouteManagerService.moveToMain();
                }
            })
        );


        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
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

    onShowHelp($event: MouseEvent): void {}
}
