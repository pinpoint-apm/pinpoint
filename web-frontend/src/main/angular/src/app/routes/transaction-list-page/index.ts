import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AngularSplitModule } from 'angular-split';

import { SharedModule } from 'app/shared';
import { NoticeModule } from 'app/core/components/notice';
import { DataLoadIndicatorModule } from 'app/core/components/data-load-indicator';
import { StateButtonModule } from 'app/core/components/state-button';
import { TransactionTableGridModule } from 'app/core/components/transaction-table-grid';
import { TransactionListBottomContentsModule } from 'app/core/components/transaction-list-bottom-contents';

import { TransactionListPageComponent } from './transaction-list-page.component';
import { routing } from './transaction-list-page.routing';
import { SideNavigationBarModule } from 'app/core/components/side-navigation-bar';

@NgModule({
    declarations: [
        TransactionListPageComponent
    ],
    imports: [
        SideNavigationBarModule,
        AngularSplitModule,
        SharedModule,
        NoticeModule,
        DataLoadIndicatorModule,
        StateButtonModule,
        TransactionTableGridModule,
        TransactionListBottomContentsModule,
        RouterModule.forChild(routing)
    ],
    exports: [],
    providers: []
})
export class TransactionListPageModule {}
