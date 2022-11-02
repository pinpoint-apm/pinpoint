import { Component, OnDestroy, OnInit } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { switchMap, takeUntil } from 'rxjs/operators';

import { MessageQueueService, MESSAGE_TO, NewUrlStateNotificationService } from 'app/shared/services';
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
        private messageQueueService: MessageQueueService,
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

    onSelectUrlInfo(url: string): void {
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.SELECT_URL_INFO,
            param: url
        })
    }
}
