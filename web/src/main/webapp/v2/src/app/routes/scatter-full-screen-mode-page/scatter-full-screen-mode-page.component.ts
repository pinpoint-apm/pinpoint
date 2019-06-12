import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { UrlPathId } from 'app/shared/models';
import { NewUrlStateNotificationService, WebAppSettingDataService } from 'app/shared/services';

@Component({
    selector: 'pp-scatter-full-screen-mode-page',
    templateUrl: './scatter-full-screen-mode-page.component.html',
    styleUrls: ['./scatter-full-screen-mode-page.component.css']
})
export class ScatterFullScreenModePageComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    applicationImgPath: string;
    applicationName: string;
    selectedAgent: string;

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private webAppSettingDataService: WebAppSettingDataService,
    ) {}

    ngOnInit() {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            this.applicationName = urlService.getPathValue(UrlPathId.APPLICATION).getApplicationName();
            if (urlService.hasValue(UrlPathId.AGENT_ID)) {
                this.selectedAgent = urlService.getPathValue(UrlPathId.AGENT_ID);
            } else {
                this.selectedAgent = 'All';
            }
            this.applicationImgPath = this.webAppSettingDataService.getIconImagePath() + urlService.getPathValue(UrlPathId.APPLICATION).getServiceType() + this.webAppSettingDataService.getImageExt();
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
}
