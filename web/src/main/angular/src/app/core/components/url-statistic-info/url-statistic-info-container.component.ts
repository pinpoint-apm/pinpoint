import { Component, OnDestroy, OnInit } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { switchMap, takeUntil } from 'rxjs/operators';

import { NewUrlStateNotificationService } from 'app/shared/services';
import { UrlStatisticInfoDataService } from './url-statistic-info-data.service';
import { UrlPathId } from 'app/shared/models';

@Component({
    selector: 'pp-url-statistic-info-container',
    templateUrl: './url-statistic-info-container.component.html',
    styleUrls: ['./url-statistic-info-container.component.css']
})
export class UrlStatisticInfoContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    data$: Observable<IUrlStatInfoData>;

    constructor(
        private urlStatisticInfoDataService: UrlStatisticInfoDataService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
    ) { }

    ngOnInit() {
        this.data$ = this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            switchMap((urlService: NewUrlStateNotificationService) => {
                const from = urlService.getStartTimeToNumber();
                const to = urlService.getEndTimeToNumber();
                const applicationName = urlService.getPathValue(UrlPathId.APPLICATION).getApplicationName();
                const agentId = urlService.getPathValue(UrlPathId.AGENT_ID) || '';
                const params = {from, to, applicationName, agentId};

                return this.urlStatisticInfoDataService.getData(params);
                
            })
            // TODO: Add error handling
        );
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    onSelectUrlInfo(urlInfo: string): void {
        // TODO: Handle it
    }
}
