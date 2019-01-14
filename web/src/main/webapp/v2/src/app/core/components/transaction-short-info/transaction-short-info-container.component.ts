import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { filter } from 'rxjs/operators';
import { StoreHelperService } from 'app/shared/services';

@Component({
    selector: 'pp-transaction-short-info-container',
    templateUrl: './transaction-short-info-container.component.html',
    styleUrls: ['./transaction-short-info-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransactionShortInfoContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    path: string;
    agentId: string;
    transactionId: string;
    applicationName: string;
    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private storeHelperService: StoreHelperService) {}
    ngOnInit() {
        this.connectStore();
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private connectStore(): void {
        this.storeHelperService.getTransactionDetailData(this.unsubscribe).pipe(
            filter((transactionDetailInfo: ITransactionDetailData) => {
                return transactionDetailInfo ? true : false;
            })
        ).subscribe((transactionDetailInfo: ITransactionDetailData) => {
            this.path = transactionDetailInfo.applicationName;
            this.agentId = transactionDetailInfo.agentId;
            this.transactionId = transactionDetailInfo.transactionId;
            this.applicationName = transactionDetailInfo.applicationId;
            this.changeDetectorRef.detectChanges();
        });
    }
}
