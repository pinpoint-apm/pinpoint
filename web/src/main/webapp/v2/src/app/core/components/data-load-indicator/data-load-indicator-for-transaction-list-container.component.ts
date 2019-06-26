import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { takeUntil, filter, take } from 'rxjs/operators';

import { UrlPathId } from 'app/shared/models';
import { NewUrlStateNotificationService, StoreHelperService } from 'app/shared/services';
import { TransactionMetaDataService } from 'app/core/components/transaction-table-grid/transaction-meta-data.service';

@Component({
    selector: 'pp-data-load-indicator-for-transaction-list-container',
    templateUrl: './data-load-indicator-for-transaction-list-container.component.html',
    styleUrls: ['./data-load-indicator-for-transaction-list-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DataLoadIndicatorForTransactionListContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<void> = new Subject();
    timezone$: Observable<string>;
    dateFormat$: Observable<string>;
    rangeValue: number[];
    selectedRangeValue: number[];

    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private storeHelperService: StoreHelperService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private transactionMetaDataService: TransactionMetaDataService
    ) {}
    ngOnInit() {
        this.connectStore();
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            filter((urlService: NewUrlStateNotificationService) => {
                return urlService.hasValue(UrlPathId.END_TIME, UrlPathId.PERIOD);
            }),
            take(1)
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            const endTime = urlService.getEndTimeToNumber();
            this.rangeValue = [urlService.getStartTimeToNumber(), endTime];
            this.selectedRangeValue = [endTime, endTime];
            this.connectMetaDataService();
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private connectStore(): void {
        this.timezone$ = this.storeHelperService.getTimezone(this.unsubscribe);
        this.dateFormat$ = this.storeHelperService.getDateFormat(this.unsubscribe, 7);
    }
    private connectMetaDataService(): void {
        this.transactionMetaDataService.onTransactionDataRange$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((range: number[]) => {
            this.selectedRangeValue = range;
            this.changeDetectorRef.detectChanges();
        });
    }
}
