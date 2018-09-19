import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';

import { UrlPathId } from 'app/shared/models';
import { RouteInfoCollectorService, WebAppSettingDataService, NewUrlStateNotificationService } from 'app/shared/services';

@Component({
    selector: 'pp-real-time',
    templateUrl: './real-time-page.component.html',
    styleUrls: ['./real-time-page.component.css']
})
export class RealTimePageComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    applicationImgPath: string;
    applicationName: string;
    constructor(
        private routeInfoCollectorService: RouteInfoCollectorService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private webAppSettingDataService: WebAppSettingDataService
    ) {}
    ngOnInit() {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter((urlService: NewUrlStateNotificationService) => {
                return urlService.hasValue(UrlPathId.APPLICATION);
            }
        )).subscribe((urlService: NewUrlStateNotificationService) => {
            this.applicationName = urlService.getPathValue(UrlPathId.APPLICATION).getApplicationName();
            this.applicationImgPath = this.webAppSettingDataService.getIconImagePath() + urlService.getPathValue(UrlPathId.APPLICATION).getServiceType() + this.webAppSettingDataService.getImageExt();
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
}
