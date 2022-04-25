import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';

import { NewUrlStateNotificationService, UrlRouteManagerService, WebAppSettingDataService } from 'app/shared/services';
import { UrlPathId } from 'app/shared/models';

@Component({
    selector: 'pp-metric-page',
    templateUrl: './metric-page.component.html',
    styleUrls: ['./metric-page.component.css']
})

export class MetricPageComponent implements OnInit {
    showSideMenu$: Observable<boolean>;
    sideNavigationUI: boolean;

    showMetric$: Observable<boolean>;

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

        this.sideNavigationUI = this.webAppSettingDataService.getExperimentalOption('sideNavigationUI');
        this.showSideMenu$ = this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            map((urlService: NewUrlStateNotificationService) => {
                return urlService.isRealTimeMode() || urlService.hasValue(UrlPathId.END_TIME);
            })
        );
    }

    onShowHelp($event: MouseEvent): void {}
}
