
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { TransactionShortInfoModule } from 'app/core/components/transaction-short-info';
import { TransactionDetailMenuModule } from 'app/core/components/transaction-detail-menu';
import { TransactionSearchModule } from 'app/core/components/transaction-search';
import { CallTreeModule } from 'app/core/components/call-tree';
import { ServerMapModule } from 'app/core/components/server-map';
import { TransactionTimelineModule } from 'app/core/components/transaction-timeline';
import { SyntaxHighlightPopupModule } from 'app/core/components/syntax-highlight-popup';
import { TransactionViewBottomContentsContainerComponent } from './transaction-view-bottom-contents-container.component';

@NgModule({
    declarations: [
        TransactionViewBottomContentsContainerComponent
    ],
    imports: [
        SharedModule,
        TransactionShortInfoModule,
        TransactionDetailMenuModule,
        TransactionSearchModule,
        ServerMapModule,
        TransactionTimelineModule,
        CallTreeModule,
        SyntaxHighlightPopupModule
    ],
    exports: [
        TransactionViewBottomContentsContainerComponent
    ],
    providers: []
})
export class TransactionViewBottomContentsModule { }
