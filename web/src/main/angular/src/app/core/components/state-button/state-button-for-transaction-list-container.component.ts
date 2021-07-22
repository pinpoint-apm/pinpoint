import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { TransactionMetaDataService } from 'app/core/components/transaction-table-grid/transaction-meta-data.service';
import { BUTTON_STATE } from './state-button.component';
import { AnalyticsService, TRACKED_EVENT_LIST, WebAppSettingDataService } from 'app/shared/services';

@Component({
    selector: 'pp-state-button-for-transaction-list-container',
    templateUrl: './state-button-for-transaction-list-container.component.html',
    styleUrls: ['./state-button-for-transaction-list-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class StateButtonForTransactionListContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    countInfo = [0, 0];
    showCountInfo = true;
    currentState = BUTTON_STATE.MORE;

    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private transactionMetaDataService: TransactionMetaDataService,
        private analyticsService: AnalyticsService,
        private webAppSettingDataService: WebAppSettingDataService,
    ) {}

    ngOnInit() {
        this.showCountInfo = !this.webAppSettingDataService.getExperimentalOption('scatterScan');
        this.transactionMetaDataService.onTransactionDataCount$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((counter: number[]) => {
            this.countInfo = counter.concat();
            this.currentState = this.isLoadCompleted() ? BUTTON_STATE.DONE : BUTTON_STATE.MORE;
            this.changeDetectorRef.detectChanges();
        });

        // * For scatter scanning v2
        this.transactionMetaDataService.onTransactionDataFecthState$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((isCompleted: boolean) => {
            this.currentState = isCompleted ? BUTTON_STATE.DONE : BUTTON_STATE.MORE;
            this.changeDetectorRef.detectChanges();
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private isLoadCompleted(): boolean {
        return this.countInfo[0] === this.countInfo[1];
    }

    onChangeState(state: string) {
        this.transactionMetaDataService.loadData();
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.LOAD_MORE_TRANSACTION_LIST);
    }
}
