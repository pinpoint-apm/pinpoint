import {
    Component,
    OnInit,
    OnDestroy,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    ComponentFactoryResolver, Injector
} from '@angular/core';
import { EMPTY, Subject } from 'rxjs';
import { filter, switchMap, catchError, takeUntil } from 'rxjs/operators';

import {
    StoreHelperService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    MessageQueueService,
    MESSAGE_TO,
    TransactionDetailDataService,
    DynamicPopupService,
    NewUrlStateNotificationService,
} from 'app/shared/services';
import { Actions } from 'app/shared/store/reducers';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup/server-error-popup-container.component';
import { UrlQuery } from 'app/shared/models';

@Component({
    selector: 'pp-transaction-timeline-v2-container',
    templateUrl: './transaction-timeline-v2-container.component.html',
    styleUrls: ['./transaction-timeline-v2-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransactionTimelineV2ContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    applicationName: string;
    traceViewerDataURL: string;

    constructor(
        private storeHelperService: StoreHelperService,
        private transactionDetailDataService: TransactionDetailDataService, // todo change to new service
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
        private messageQueueService: MessageQueueService,
        private analyticsService: AnalyticsService,
        private cd: ChangeDetectorRef
    ) {}

    ngOnInit() {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter((urlService: NewUrlStateNotificationService) => urlService.hasValue(UrlQuery.TRANSACTION_INFO)),
            switchMap((urlService: NewUrlStateNotificationService) => {
                const {agentId, spanId, traceId, collectorAcceptTime} = JSON.parse(urlService.getQueryValue(UrlQuery.TRANSACTION_INFO));

                return this.transactionDetailDataService.getTimelineData(agentId, spanId, traceId, collectorAcceptTime).pipe(
                    catchError((error: IServerError) => {
                        this.dynamicPopupService.openPopup({
                            data: {
                                title: 'Error',
                                contents: error
                            },
                            component: ServerErrorPopupContainerComponent
                        }, {
                            resolver: this.componentFactoryResolver,
                            injector: this.injector
                        });
                        this.cd.detectChanges();
                        return EMPTY;
                    }),
                );
            })
        ).subscribe((transactionTimelineInfo: ITransactionTimelineData) => {
            this.applicationName = transactionTimelineInfo.applicationId;
            this.traceViewerDataURL = transactionTimelineInfo.traceViewerDataURL;
            this.cd.detectChanges();
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    onSelectTransaction(id: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SELECT_TRANSACTION_IN_TIMELINE);
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.TRANSACTION_TIMELINE_SELECT_TRANSACTION,
            param: id
        });
        this.storeHelperService.dispatch(new Actions.ChangeTransactionViewType('callTree'));
    }
}
