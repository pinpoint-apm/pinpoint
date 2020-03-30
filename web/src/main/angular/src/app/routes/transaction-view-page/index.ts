import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AngularSplitModule } from 'angular-split';

import { SharedModule } from 'app/shared';
import { NoticeModule } from 'app/core/components/notice';
import { ConfigurationIconModule } from 'app/core/components/configuration-icon';
import { TransactionShortInfoModule } from 'app/core/components/transaction-short-info';
import { TransactionViewTopContentsModule } from 'app/core/components/transaction-view-top-contents';
import { TransactionViewBottomContentsModule } from 'app/core/components/transaction-view-bottom-contents';
import { TransactionViewPageComponent } from './transaction-view-page.component';
import { routing } from './transaction-view-page.routing';
import { ServerErrorPopupModule } from 'app/core/components/server-error-popup';

@NgModule({
    declarations: [
        TransactionViewPageComponent
    ],
    imports: [
        AngularSplitModule,
        SharedModule,
        NoticeModule,
        ConfigurationIconModule,
        TransactionShortInfoModule,
        TransactionViewTopContentsModule,
        TransactionViewBottomContentsModule,
        ServerErrorPopupModule,
        RouterModule.forChild(routing)
    ],
    exports: [
    ],
    providers: []
})
export class TransactionViewPageModule {}
