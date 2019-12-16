
import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { TransactionDetailMenuComponent } from './transaction-detail-menu.component';
import { TransactionDetailMenuContainerComponent } from './transaction-detail-menu-container.component';
import { TransactionDetailMenuForDetailContainerComponent } from './transaction-detail-menu-for-detail-container.component';
import { MessagePopupModule } from 'app/core/components/message-popup';

@NgModule({
    declarations: [
        TransactionDetailMenuComponent,
        TransactionDetailMenuContainerComponent,
        TransactionDetailMenuForDetailContainerComponent
    ],
    imports: [
        SharedModule,
        MessagePopupModule
    ],
    exports: [
        TransactionDetailMenuContainerComponent,
        TransactionDetailMenuForDetailContainerComponent
    ],
    providers: []
})
export class TransactionDetailMenuModule { }
