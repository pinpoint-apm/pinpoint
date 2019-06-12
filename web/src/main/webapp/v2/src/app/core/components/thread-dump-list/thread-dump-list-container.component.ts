import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { takeUntil, filter, switchMap } from 'rxjs/operators';

import { UrlPathId } from 'app/shared/models';
import { StoreHelperService, NewUrlStateNotificationService, MessageQueueService, MESSAGE_TO } from 'app/shared/services';
import { ActiveThreadDumpListDataService, IActiveThreadDump } from './active-thread-dump-list-data.service';
import { IThreadDumpData } from './thread-dump-list.component';


@Component({
    selector: 'pp-thread-dump-list-container',
    templateUrl: './thread-dump-list-container.component.html',
    styleUrls: ['./thread-dump-list-container.component.css']
})
export class ThreadDumpListContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    private applicationName: string;
    private agentId: string;
    rowData: IThreadDumpData[] = [];
    serverResponseError = true;
    hasErrorResponse = false;
    errorMessage: string;
    showLoading = true;
    loaded = false;
    timezone$: Observable<string>;
    dateFormat$: Observable<string>;
    constructor(
        private storeHelperService: StoreHelperService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private activeThreadDumpListDataService: ActiveThreadDumpListDataService,
        private messageQueueService: MessageQueueService
    ) {}
    ngOnInit() {
        this.connectStore();
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter((urlService: NewUrlStateNotificationService) => {
                return urlService.hasValue(UrlPathId.APPLICATION, UrlPathId.AGENT_ID);
            }),
            switchMap((urlService: NewUrlStateNotificationService) => {
                this.applicationName = urlService.getPathValue(UrlPathId.APPLICATION).getApplicationName();
                this.agentId = urlService.getPathValue(UrlPathId.AGENT_ID);
                return this.activeThreadDumpListDataService.getData(this.applicationName, this.agentId);
            })
        ).subscribe((data: any) => {
            if (data.code === -1)  {
                this.serverResponseError = true;
                this.hasErrorResponse = true;
                this.errorMessage = data.message;
            } else {
                this.rowData = data.message.threadDumpData.map((threadDump: IActiveThreadDump, index: number) => {
                    return <IThreadDumpData>{
                        index: index + 1,
                        id: threadDump.threadId,
                        name: threadDump.threadName,
                        state: threadDump.threadState,
                        startTime: threadDump.startTime,
                        exec: threadDump.execTime,
                        sampled: threadDump.sampled,
                        path: threadDump.entryPoint,
                        transactionId: threadDump.transactionId,
                        localTraceId: threadDump.localTraceId
                    };
                });
            }
            this.showLoading = false;
        }, (error: IServerErrorFormat) => {
            this.serverResponseError = false;
            this.hasErrorResponse = true;
            this.errorMessage = error.exception.message;
            this.showLoading = false;
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private connectStore(): void {
        this.timezone$ = this.storeHelperService.getTimezone(this.unsubscribe);
        this.dateFormat$ = this.storeHelperService.getDateFormat(this.unsubscribe, 2);
    }
    hasData(): boolean {
        return this.rowData.length > 0;
    }
    hasError(): boolean {
        return this.hasErrorResponse;
    }
    onSelectThread(param: any): void {
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.THREAD_DUMP_SET_PARAM,
            param: [param]
        });
    }
}
