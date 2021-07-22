import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import {
    UrlRouteManagerService,
    NewUrlStateNotificationService,
    GutterEventService,
    StoreHelperService,
    AnalyticsService, TRACKED_EVENT_LIST
} from 'app/shared/services';
import { Actions } from 'app/shared/store/reducers';
import { UrlPath, UrlPathId, UrlQuery } from 'app/shared/models';
import { IGridData } from './transaction-table-grid.component';
import { TransactionMetaDataService } from './transaction-meta-data.service';

@Component({
    selector: 'pp-transaction-table-grid-container',
    templateUrl: './transaction-table-grid-container.component.html',
    styleUrls: ['./transaction-table-grid-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransactionTableGridContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    areaResized: any;
    selectedTransactionId: string;
    transactionData: ITransactionMetaData[] = [];
    transactionDataForAgGrid: IGridData[];
    transactionAddedDataForAgGrid: IGridData[];
    transactionIndex = 1;
    timezone: string;
    dateFormat: string;
    dataEmptyText: string;

    constructor(
        private storeHelperService: StoreHelperService,
        private urlRouteManagerService: UrlRouteManagerService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private transactionMetaDataService: TransactionMetaDataService,
        private gutterEventService: GutterEventService,
        private translateService: TranslateService,
        private analyticsService: AnalyticsService,
        private cd: ChangeDetectorRef
    ) {}

    ngOnInit() {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter((urlService: NewUrlStateNotificationService) => urlService.hasValue(UrlQuery.DRAG_INFO))
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            if (urlService.hasValue(UrlQuery.TRANSACTION_INFO)) {
                const {agentId, spanId, traceId, collectorAcceptTime} = JSON.parse(urlService.getQueryValue(UrlQuery.TRANSACTION_INFO));

                this.selectedTransactionId = `${agentId}${spanId}${traceId}${collectorAcceptTime}`;
                this.dispatchTransaction();
            }

            if (this.transactionData.length === 0) {
                this.transactionMetaDataService.loadData();
            }
        });

        this.gutterEventService.onGutterResized$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((params: any) => {
            this.areaResized = params;
        });

        this.connectStore();
        this.connectMetaDataService();
        this.initI18nText();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private connectStore(): void {
        this.storeHelperService.getTimezone(this.unsubscribe).subscribe((timezone: string) => {
            this.timezone = timezone;
        });
        this.storeHelperService.getDateFormat(this.unsubscribe, 2).subscribe((dateFormat: string) => {
            this.dateFormat = dateFormat;
        });
    }

    private connectMetaDataService(): void {
        this.transactionMetaDataService.onTransactionDataLoad$.pipe(
            takeUntil(this.unsubscribe),
            filter((responseData: ITransactionMetaData[]) => {
                if (responseData.length === 0) {
                    this.transactionDataForAgGrid = [];
                    return false;
                } else {
                    return true;
                }
            }),
        ).subscribe((responseData: ITransactionMetaData[]) => {
            this.transactionData = this.transactionData.concat(responseData || []);
            if (this.transactionDataForAgGrid) {
                this.transactionAddedDataForAgGrid = this.makeGridData(responseData);
            } else {
                this.transactionDataForAgGrid = this.makeGridData(responseData);
                this.dispatchTransaction();
            }
            this.cd.detectChanges();
        });
    }

    private initI18nText(): void {
        this.translateService.get('COMMON.NO_DATA').subscribe((dataEmptyText: string) => {
           this.dataEmptyText = dataEmptyText;
        });
    }

    private makeGridData(transactionData: ITransactionMetaData[]): IGridData[] {
        return transactionData.map((data: ITransactionMetaData) => {
            return this.makeRow(data);
        });
    }

    private makeRow(gridData: ITransactionMetaData): IGridData {
        return {
            id: this.transactionIndex++,
            startTime: gridData.startTime,
            path: gridData.application,
            endpoint: gridData.endpoint,
            responseTime: gridData.elapsed,
            exception: gridData.exception,
            agentId: gridData.agentId,
            agentName: gridData.agentName,
            clientIp:  gridData.remoteAddr,
            traceId: gridData.traceId,
            spanId: gridData.spanId,
            collectorAcceptTime: gridData.collectorAcceptTime
        } as IGridData;
    }

    private findTransaction(transactionId: string): ITransactionMetaData {
        return this.transactionData.find(({agentId, spanId, traceId, collectorAcceptTime}: ITransactionMetaData) => {
            return `${agentId}${spanId}${traceId}${collectorAcceptTime}` === transactionId;
        });
    }

    private dispatchTransaction(): void {
        if (this.selectedTransactionId) {
            const transaction = this.findTransaction(this.selectedTransactionId);

            if (transaction) {
                this.storeHelperService.dispatch(new Actions.UpdateTransactionData(transaction));
            }
        }
    }

    onSelectTransaction({agentId, spanId, traceId, collectorAcceptTime, elapsed}: {[key: string]: any}): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SELECT_TRANSACTION);
        this.urlRouteManagerService.moveOnPage({
            url: [
                UrlPath.TRANSACTION_LIST,
                this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime(),
            ],
            queryParams: {
                [UrlQuery.TRANSACTION_INFO]: {agentId, spanId, traceId, collectorAcceptTime, elapsed}
            }
        });
    }

    onOpenTransactionView(transactionShortInfo: { agentId: string, traceId: string, collectorAcceptTime: number, spanId: string }): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_TRANSACTION_VIEW_PAGE_WITH_ICON);
        this.urlRouteManagerService.openPage({
            path: [
                UrlPath.TRANSACTION_VIEW,
                transactionShortInfo.agentId,
                transactionShortInfo.traceId,
                transactionShortInfo.collectorAcceptTime + '',
                transactionShortInfo.spanId,
            ]
        });
    }
}
