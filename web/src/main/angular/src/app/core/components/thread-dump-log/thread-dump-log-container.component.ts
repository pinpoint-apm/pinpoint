import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, of } from 'rxjs';
import { takeUntil, filter, map, tap, switchMap, catchError } from 'rxjs/operators';

import { UrlPathId } from 'app/shared/models';
import { NewUrlStateNotificationService, MessageQueueService, MESSAGE_TO } from 'app/shared/services';
import { ActiveThreadDumpDetailInfoDataService } from './active-thread-dump-detail-info-data.service';

@Component({
    selector: 'pp-thread-dump-log-container',
    templateUrl: './thread-dump-log-container.component.html',
    styleUrls: ['./thread-dump-log-container.component.css'],
})
export class ThreadDumpLogContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private applicationName: string;
    private agentId: string;

    showLoading = false;
    logData: string;

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private messageQueueService: MessageQueueService,
        private activeThreadDumpDetailInfoDataService: ActiveThreadDumpDetailInfoDataService
    ) {}

    ngOnInit() {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter((urlService: NewUrlStateNotificationService) => urlService.hasValue(UrlPathId.APPLICATION, UrlPathId.AGENT_ID))
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            this.applicationName = urlService.getPathValue(UrlPathId.APPLICATION).getApplicationName();
            this.agentId = urlService.getPathValue(UrlPathId.AGENT_ID);
        });

        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.THREAD_DUMP_SET_PARAM).pipe(
            tap(() => this.showLoading = true),
            switchMap(({threadName, localTraceId}: any) => {
                return this.activeThreadDumpDetailInfoDataService.getData(this.applicationName, this.agentId, threadName, localTraceId);
            }),
            map(({code, message}: {[key: string]: any}) => {
                return code === -1 ? message
                    : message.threadDumpData.length > 0 ? message.threadDumpData[0].detailMessage
                    : 'There is no message(may be completed)';
            }),
            catchError((error: IServerErrorFormat) => {
                return of(error.exception.message);
            }),
        ).subscribe((message: string) => {
            this.showLoading = false;
            this.logData = message;
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
}
