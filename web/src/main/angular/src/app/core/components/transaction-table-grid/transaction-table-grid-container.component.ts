import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';

import {
    UrlRouteManagerService,
    NewUrlStateNotificationService,
    GutterEventService,
    StoreHelperService,
    AnalyticsService, TRACKED_EVENT_LIST
} from 'app/shared/services';
import { Actions } from 'app/shared/store';
import { UrlPath, UrlPathId } from 'app/shared/models';
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
    selectedTraceId: string;
    transactionData: ITransactionMetaData[] = [];
    transactionDataForAgGrid: IGridData[];
    transactionAddedDataForAgGrid: IGridData[];
    transactionIndex = 1;
    timezone: string;
    dateFormat: string;

    constructor(
        private storeHelperService: StoreHelperService,
        private urlRouteManagerService: UrlRouteManagerService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private transactionMetaDataService: TransactionMetaDataService,
        private gutterEventService: GutterEventService,
        private analyticsService: AnalyticsService,
        private cd: ChangeDetectorRef
    ) {}

    ngOnInit() {
        this.connectStore();
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            if (urlService.hasValue(UrlPathId.TRANSACTION_INFO)) {
                this.selectedTraceId = urlService.getPathValue(UrlPathId.TRANSACTION_INFO).replace(/(.*)-\d*-\d*$/, '$1');
                this.dispatchTransaction();
            }
            if (this.transactionData.length === 0) {
                this.transactionMetaDataService.loadData();
            }
        });
        this.connectMetaDataService();
        this.gutterEventService.onGutterResized$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((params: any) => {
            this.areaResized = params;
        });
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
            filter((responseData: any) => responseData.length > 0)
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
            responseTime: gridData.elapsed,
            exception: gridData.exception,
            agentId: gridData.agentId,
            clientIp:  gridData.remoteAddr,
            traceId: gridData.traceId,
            spanId: gridData.spanId,
            collectorAcceptTime: gridData.collectorAcceptTime
        } as IGridData;
    }

    private findTransaction(traceId: string): ITransactionMetaData {
        for (let i = 0; i < this.transactionData.length; i++) {
            if (this.transactionData[i].traceId === traceId) {
                return this.transactionData[i];
            }
        }

        return null;
    }

    private dispatchTransaction(): void {
        if (this.selectedTraceId) {
            const transaction = this.findTransaction(this.selectedTraceId);

            if (transaction) {
                this.storeHelperService.dispatch(new Actions.UpdateTransactionData(transaction));
            }
        }
    }

    onSelectTransaction(transactionShortInfo: { traceId: string, collectorAcceptTime: number, elapsed: number }): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SELECT_TRANSACTION);
        this.urlRouteManagerService.moveOnPage({
            url: [
                UrlPath.TRANSACTION_LIST,
                this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime(),
                `${transactionShortInfo.traceId}-${transactionShortInfo.collectorAcceptTime}-${transactionShortInfo.elapsed}`
            ]
        });
    }

    onOpenTransactionView(transactionShortInfo: { agentId: string, traceId: string, collectorAcceptTime: number, spanId: string }): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_TRANSACTION_VIEW);
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
