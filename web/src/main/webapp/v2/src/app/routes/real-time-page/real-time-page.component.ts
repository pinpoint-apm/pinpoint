import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { UrlPathId } from 'app/shared/models';
import { WebAppSettingDataService, NewUrlStateNotificationService } from 'app/shared/services';

@Component({
    selector: 'pp-real-time',
    templateUrl: './real-time-page.component.html',
    styleUrls: ['./real-time-page.component.css']
})
export class RealTimePageComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    applicationImgPath: string;
    applicationName: string;
    enableRealTime$: Observable<boolean>;

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private webAppSettingDataService: WebAppSettingDataService
    ) {}

    ngOnInit() {
        this.enableRealTime$ = this.webAppSettingDataService.useActiveThreadChart();
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            this.applicationName = urlService.getPathValue(UrlPathId.APPLICATION).getApplicationName();
            this.applicationImgPath = this.webAppSettingDataService.getIconImagePath() + urlService.getPathValue(UrlPathId.APPLICATION).getServiceType() + this.webAppSettingDataService.getImageExt();
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
}
