import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AngularSplitModule } from 'angular-split';

import { SharedModule } from 'app/shared';
import { NoticeModule } from 'app/core/components/notice';
import { DataLoadIndicatorModule } from 'app/core/components/data-load-indicator';
import { StateButtonModule } from 'app/core/components/state-button';
import { ConfigurationIconModule } from 'app/core/components/configuration-icon';
import { TransactionTableGridModule } from 'app/core/components/transaction-table-grid';
import { TransactionListBottomContentsModule } from 'app/core/components/transaction-list-bottom-contents';

import { TransactionListEmptyComponent } from './transaction-list-empty.component';
import { TransactionListPageComponent } from './transaction-list-page.component';
import { routing } from './transaction-list-page.routing';

@NgModule({
    declarations: [
        TransactionListEmptyComponent,
        TransactionListPageComponent
    ],
    imports: [
        AngularSplitModule,
        SharedModule,
        NoticeModule,
        DataLoadIndicatorModule,
        StateButtonModule,
        ConfigurationIconModule,
        TransactionTableGridModule,
        TransactionListBottomContentsModule,
        RouterModule.forChild(routing)
    ],
    exports: [],
    providers: []
})
export class TransactionListPageModule {}
