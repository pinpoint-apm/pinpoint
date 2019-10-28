
import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { StateButtonComponent } from './state-button.component';
import { StateButtonForFilteredMapContainerComponent } from './state-button-for-filtered-map-container.component';
import { StateButtonForTransactionListContainerComponent } from './state-button-for-transaction-list-container.component';
import { TransactionTableGridModule } from 'app/core/components/transaction-table-grid';

@NgModule({
    declarations: [
        StateButtonComponent,
        StateButtonForFilteredMapContainerComponent,
        StateButtonForTransactionListContainerComponent
    ],
    imports: [
        SharedModule,
        TransactionTableGridModule
    ],
    exports: [
        StateButtonForFilteredMapContainerComponent,
        StateButtonForTransactionListContainerComponent
    ],
    providers: []
})
export class StateButtonModule { }
