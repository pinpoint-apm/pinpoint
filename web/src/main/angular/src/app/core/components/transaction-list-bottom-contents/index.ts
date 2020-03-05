import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { TransactionShortInfoModule } from 'app/core/components/transaction-short-info';
import { TransactionDetailMenuModule } from 'app/core/components/transaction-detail-menu';
import { TransactionSearchModule } from 'app/core/components/transaction-search';
import { CallTreeModule } from 'app/core/components/call-tree';
import { ServerMapModule } from 'app/core/components/server-map';
import { TransactionTimelineModule } from 'app/core/components/transaction-timeline';
import { TransactionTableGridModule } from 'app/core/components/transaction-table-grid';
import { SyntaxHighlightPopupModule } from 'app/core/components/syntax-highlight-popup';
import { TransactionListBottomContentsContainerComponent } from './transaction-list-bottom-contents-container.component';
import { HelpViewerPopupModule } from 'app/core/components/help-viewer-popup';
import { ServerErrorPopupModule } from 'app/core/components/server-error-popup';

@NgModule({
    declarations: [
        TransactionListBottomContentsContainerComponent
    ],
    imports: [
        SharedModule,
        TransactionTableGridModule,
        TransactionShortInfoModule,
        TransactionDetailMenuModule,
        TransactionSearchModule,
        ServerMapModule,
        TransactionTimelineModule,
        CallTreeModule,
        SyntaxHighlightPopupModule,
        HelpViewerPopupModule,
        ServerErrorPopupModule
    ],
    exports: [
        TransactionListBottomContentsContainerComponent
    ],
    providers: []
})
export class TransactionListBottomContentsModule { }
