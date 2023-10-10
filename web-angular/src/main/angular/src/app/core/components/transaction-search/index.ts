
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { TransactionSearchComponent } from './transaction-search.component';
import { TransactionSearchContainerComponent } from './transaction-search-container.component';
import { TransactionSearchInteractionService } from './transaction-search-interaction.service';

@NgModule({
    declarations: [
        TransactionSearchComponent,
        TransactionSearchContainerComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [
        TransactionSearchContainerComponent
    ],
    providers: [
        TransactionSearchInteractionService
    ]
})
export class TransactionSearchModule { }
