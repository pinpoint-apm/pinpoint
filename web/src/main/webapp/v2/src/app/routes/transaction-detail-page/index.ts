import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { SharedModule } from 'app/shared';
import { NoticeModule } from 'app/core/components/notice';
import { ApplicationListModule } from 'app/core/components/application-list';
import { CommandGroupModule } from 'app/core/components/command-group';
import { TransactionDetailContentsModule } from 'app/core/components/transaction-detail-contents';
import { TransactionDetailPageComponent } from './transaction-detail-page.component';
import { routing } from './transaction-detail-page.routing';

@NgModule({
    declarations: [
        TransactionDetailPageComponent
    ],
    imports: [
        SharedModule,
        NoticeModule,
        ApplicationListModule,
        CommandGroupModule,
        TransactionDetailContentsModule,
        RouterModule.forChild(routing)
    ],
    exports: [],
    providers: []
})
export class TransactionDetailPageModule {}
