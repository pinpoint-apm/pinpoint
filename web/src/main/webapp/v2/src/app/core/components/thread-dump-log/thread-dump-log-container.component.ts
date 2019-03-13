import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';

import { UrlPathId } from 'app/shared/models';
import { NewUrlStateNotificationService, MessageQueueService, MESSAGE_TO } from 'app/shared/services';
import { ActiveThreadDumpDetailInfoDataService } from './active-thread-dump-detail-info-data.service';

@Component({
    selector: 'pp-thread-dump-log-container',
    templateUrl: './thread-dump-log-container.component.html',
    styleUrls: ['./thread-dump-log-container.component.css'],
})
export class ThreadDumpLogContainerComponent implements OnInit, OnDestroy {
    @ViewChild('logDisplay') target: ElementRef;
    private unsubscribe: Subject<null> = new Subject();
    private applicationName: string;
    private agentId: string;
    showLoading = false;
    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private messageQueueService: MessageQueueService,
        private activeThreadDumpDetailInfoDataService: ActiveThreadDumpDetailInfoDataService
    ) {}
    ngOnInit() {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter((urlService: NewUrlStateNotificationService) => {
                return urlService.hasValue(UrlPathId.APPLICATION, UrlPathId.AGENT_ID);
            }
        )).subscribe((urlService: NewUrlStateNotificationService) => {
            this.applicationName = urlService.getPathValue(UrlPathId.APPLICATION).getApplicationName();
            this.agentId = urlService.getPathValue(UrlPathId.AGENT_ID);
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.THREAD_DUMP_SET_PARAM).subscribe((param: any[]) => {
            const threadParam = param[0] as {
                threadName: string;
                localTraceId: number;
            };
            this.showLoading = true;
            this.loadData(threadParam.threadName, threadParam.localTraceId);
        });
    }
    private loadData(threadName: string, localTraceId: number): void {
        this.activeThreadDumpDetailInfoDataService.getData(this.applicationName, this.agentId, threadName, localTraceId).subscribe((data: any) => {
            let msg = '';
            if (data.code === -1) {
                msg = data.message;
            } else {
                if (data.message.threadDumpData.length > 0) {
                    msg = data.message.threadDumpData[0].detailMessage;
                } else {
                    msg = 'There is no message( may be completed )';
                }
            }
            this.target.nativeElement.value = msg;
            this.showLoading = false;
        }, (error: IServerErrorFormat) => {
            this.target.nativeElement.value = error.exception.message;
            this.showLoading = false;
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
}
