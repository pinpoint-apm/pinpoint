
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';

import { TransactionIdSearchContainerComponent } from './transaction-id-search-container.component';

@NgModule({
    declarations: [
        TransactionIdSearchContainerComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [
        TransactionIdSearchContainerComponent,
    ],
    entryComponents: [
        TransactionIdSearchContainerComponent,
    ],
    providers: []
})
export class TransactionIdSearchModule { }
