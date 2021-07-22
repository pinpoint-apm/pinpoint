
import { NgModule } from '@angular/core';
import { AgGridModule } from 'ag-grid-angular/main';

import { SharedModule } from 'app/shared';
import { TransactionTableGridComponent } from './transaction-table-grid.component';
import { TransactionTableGridContainerComponent } from './transaction-table-grid-container.component';
import { TransactionMetaDataService } from './transaction-meta-data.service';
import { MessagePopupModule } from 'app/core/components/message-popup';
import { ServerErrorPopupModule } from 'app/core/components/server-error-popup';

@NgModule({
    declarations: [
        TransactionTableGridComponent,
        TransactionTableGridContainerComponent
    ],
    imports: [
        AgGridModule.withComponents([]),
        SharedModule,
        MessagePopupModule,
        ServerErrorPopupModule
    ],
    exports: [
        TransactionTableGridContainerComponent
    ],
    providers: [
        TransactionMetaDataService
    ]
})
export class TransactionTableGridModule { }
