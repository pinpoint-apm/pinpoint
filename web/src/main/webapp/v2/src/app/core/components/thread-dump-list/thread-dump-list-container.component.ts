import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, Observable, of, throwError } from 'rxjs';
import { filter, switchMap } from 'rxjs/operators';

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
    private unsubscribe = new Subject<void>();

    rowData$: Observable<IThreadDumpData[]>;
    serverResponseError = true;
    hasErrorResponse = false;
    errorMessage: string;
    showLoading = true;
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
        this.rowData$ = this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            filter((urlService: NewUrlStateNotificationService) => urlService.hasValue(UrlPathId.APPLICATION, UrlPathId.AGENT_ID)),
            switchMap((urlService: NewUrlStateNotificationService) => {
                const applicationName = urlService.getPathValue(UrlPathId.APPLICATION).getApplicationName();
                const agentId = urlService.getPathValue(UrlPathId.AGENT_ID);

                return this.activeThreadDumpListDataService.getData(applicationName, agentId);
            }),
            switchMap((data: {[key: string]: any}) => {
                if (data.code === -1)  {
                    return throwError({message: data.message});
                } else {
                    return of(data.message.threadDumpData.map((threadDump: IActiveThreadDump, index: number) => {
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
                    }));
                }
            })
        );
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private connectStore(): void {
        this.timezone$ = this.storeHelperService.getTimezone(this.unsubscribe);
        this.dateFormat$ = this.storeHelperService.getDateFormat(this.unsubscribe, 2);
    }

    onSelectThread(param: any): void {
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.THREAD_DUMP_SET_PARAM,
            param
        });
    }
}
