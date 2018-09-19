import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';

import { TransactionMetaDataService } from 'app/core/components/transaction-table-grid/transaction-meta-data.service';
import { BUTTON_STATE } from './state-button.component';

@Component({
    selector: 'pp-state-button-for-transaction-list-container',
    templateUrl: './state-button-for-transaction-list-container.component.html',
    styleUrls: ['./state-button-for-transaction-list-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class StateButtonForTransactionListContainerComponent implements OnInit {
    countInfo = [0, 0];
    showCountInfo = true;
    currentState = BUTTON_STATE.MORE;
    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private transactionMetaDataService: TransactionMetaDataService
    ) {}
    ngOnInit() {
        this.transactionMetaDataService.onTransactionDataCount$.subscribe((counter: number[]) => {
            this.countInfo = counter.concat();
            this.currentState = this.isLoadCompleted() ? BUTTON_STATE.DONE : BUTTON_STATE.MORE;
            this.changeDetectorRef.detectChanges();
        });
    }
    private isLoadCompleted(): boolean {
        return this.countInfo[0] === this.countInfo[1];
    }
    onChangeState(state: string) {
        this.transactionMetaDataService.loadData();
    }
}
