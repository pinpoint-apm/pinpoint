
import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { TransactionShortInfoModule } from 'app/core/components/transaction-short-info';
import { TransactionDetailMenuModule } from 'app/core/components/transaction-detail-menu';
import { TransactionSearchModule } from 'app/core/components/transaction-search';
import { CallTreeModule } from 'app/core/components/call-tree';
import { ServerMapModule } from 'app/core/components/server-map';
import { TransactionTimelineModule } from 'app/core/components/transaction-timeline';
import { SyntaxHighlightPopupModule } from 'app/core/components/syntax-highlight-popup';
import { TransactionDetailContentsContainerComponent } from './transaction-detail-contents-container.component';
import { HelpViewerPopupModule } from 'app/core/components/help-viewer-popup';

@NgModule({
    declarations: [
        TransactionDetailContentsContainerComponent
    ],
    imports: [
        SharedModule,
        TransactionShortInfoModule,
        TransactionDetailMenuModule,
        TransactionSearchModule,
        CallTreeModule,
        ServerMapModule,
        TransactionTimelineModule,
        SyntaxHighlightPopupModule,
        HelpViewerPopupModule
    ],
    exports: [
        TransactionDetailContentsContainerComponent
    ],
    providers: []
})
export class TransactionDetailContentsModule { }
