import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { SharedModule } from 'app/shared';
import { NoticeModule } from 'app/core/components/notice';
import { TransactionDetailContentsModule } from 'app/core/components/transaction-detail-contents';
import { TransactionDetailPageComponent } from './transaction-detail-page.component';
import { routing } from './transaction-detail-page.routing';
import { ServerErrorPopupModule } from 'app/core/components/server-error-popup';
import { SideNavigationBarModule } from 'app/core/components/side-navigation-bar';

@NgModule({
    declarations: [
        TransactionDetailPageComponent
    ],
    imports: [
        SharedModule,
        NoticeModule,
        TransactionDetailContentsModule,
        ServerErrorPopupModule,
        SideNavigationBarModule,
        RouterModule.forChild(routing)
    ],
    exports: [],
    providers: []
})
export class TransactionDetailPageModule {}
