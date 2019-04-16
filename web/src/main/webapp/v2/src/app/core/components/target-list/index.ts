
import { NgModule } from '@angular/core';

import { CommonModule } from '@angular/common';
import { SharedModule } from 'app/shared';
import { TargetListComponent } from './target-list.component';
import { TargetListContainerComponent } from './target-list-container.component';
import { FilterTransactionWizardPopupModule } from 'app/core/components/filter-transaction-wizard-popup';

@NgModule({
    declarations: [
        TargetListComponent,
        TargetListContainerComponent
    ],
    imports: [
        CommonModule,
        SharedModule,
        FilterTransactionWizardPopupModule
    ],
    exports: [
        TargetListContainerComponent
    ],
    providers: []
})
export class TargetListModule { }
