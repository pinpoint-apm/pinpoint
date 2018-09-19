
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { DataLoadIndicatorForFilteredMapContainerComponent } from './data-load-indicator-for-filtered-map-container.component';
import { DataLoadIndicatorForTransactionListContainerComponent } from './data-load-indicator-for-transaction-list-container.component';
import { TransactionTableGridModule } from 'app/core/components/transaction-table-grid';

@NgModule({
    declarations: [
        DataLoadIndicatorForFilteredMapContainerComponent,
        DataLoadIndicatorForTransactionListContainerComponent
    ],
    imports: [
        SharedModule,
        TransactionTableGridModule
    ],
    exports: [
        DataLoadIndicatorForFilteredMapContainerComponent,
        DataLoadIndicatorForTransactionListContainerComponent
    ],
    providers: []
})
export class DataLoadIndicatorModule { }
