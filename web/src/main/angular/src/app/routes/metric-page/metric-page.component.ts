import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { NewUrlStateNotificationService } from 'app/shared/services';
import { UrlPathId } from 'app/shared/models';

@Component({
    selector: 'pp-metric-page',
    templateUrl: './metric-page.component.html',
    styleUrls: ['./metric-page.component.css']
})

export class MetricPageComponent implements OnInit {
    showSideMenu$: Observable<boolean>;

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
    ) {}

    ngOnInit() {
        this.showSideMenu$ = this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            map((urlService: NewUrlStateNotificationService) => {
                return urlService.isRealTimeMode() || urlService.hasValue(UrlPathId.END_TIME);
            })
        );
    }

    onShowHelp($event: MouseEvent): void {}
}
