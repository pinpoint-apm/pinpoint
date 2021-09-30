import {
    Component,
    OnInit,
    OnDestroy,
    ViewChild,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    ComponentFactoryResolver, Injector
} from '@angular/core';
import {EMPTY, forkJoin, Subject} from 'rxjs';
import {filter, switchMap, catchError, tap} from 'rxjs/operators';

import {
    StoreHelperService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    MessageQueueService,
    MESSAGE_TO,
    NewUrlStateNotificationService,
    TransactionDetailDataService,
    DynamicPopupService,
    UrlRouteManagerService
} from 'app/shared/services';
import { TransactionTimelineV2Component } from './transaction-timeline-v2.component';
import { Actions } from 'app/shared/store/reducers';
import {UrlPath, UrlPathId, UrlQuery} from "../../../shared/models";
import {ServerErrorPopupContainerComponent} from "../server-error-popup/server-error-popup-container.component";

@Component({
    selector: 'pp-transaction-timeline-v2-container',
    templateUrl: './transaction-timeline-v2-container.component.html',
    styleUrls: ['./transaction-timeline-v2-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransactionTimelineV2ContainerComponent implements OnInit, OnDestroy {
    @ViewChild(TransactionTimelineV2Component, { static: true }) transactionTimelineComponent: TransactionTimelineV2Component;

    private unsubscribe = new Subject<void>();

    applicationName: string;
    traceViewerDataURL: string;

    constructor(
        private storeHelperService: StoreHelperService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private transactionDetailDataService: TransactionDetailDataService, //todo change to new service
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
        private messageQueueService: MessageQueueService,
        private analyticsService: AnalyticsService,
        private cd: ChangeDetectorRef
    ) {}

    ngOnInit() {
        this.storeHelperService.getTransactionData(this.unsubscribe).pipe(
            filter((data: ITransactionMetaData) => !!data),
            filter(({agentId, spanId, traceId, collectorAcceptTime}: ITransactionMetaData) => !!agentId && !!spanId && !!traceId && !!collectorAcceptTime),
            switchMap(({agentId, spanId, traceId, collectorAcceptTime}: ITransactionMetaData) => {
                return forkJoin(
                    this.transactionDetailDataService.getTimelineData(agentId, spanId, traceId, collectorAcceptTime)
                ).pipe(
                    catchError((error: IServerErrorFormat) => {
                        this.dynamicPopupService.openPopup({
                            data: {
                                title: 'Error',
                                contents: error
                            },
                            component: ServerErrorPopupContainerComponent,
                            onCloseCallback: () => {
                                this.urlRouteManagerService.moveOnPage({
                                    url: [
                                        UrlPath.TRANSACTION_LIST,
                                        this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                                        this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                                        this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime()
                                    ],
                                    queryParams: {
                                        [UrlQuery.TRANSACTION_INFO]: null
                                    }
                                });
                            }
                        }, {
                            resolver: this.componentFactoryResolver,
                            injector: this.injector
                        });
                        this.cd.detectChanges();
                        return EMPTY;
                    }),
                );
            })
        ).subscribe(([transactionTimelineInfo]: [ITransactionTimelineData]) => {
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
