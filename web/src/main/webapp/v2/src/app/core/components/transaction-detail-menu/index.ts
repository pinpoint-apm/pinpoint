
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

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
        CommonModule,
        MessagePopupModule
    ],
    exports: [
        TransactionDetailMenuContainerComponent,
        TransactionDetailMenuForDetailContainerComponent
    ],
    providers: []
})
export class TransactionDetailMenuModule { }
