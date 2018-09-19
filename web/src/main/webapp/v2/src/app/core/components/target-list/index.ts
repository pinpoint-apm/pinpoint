
import { NgModule } from '@angular/core';

import { CommonModule } from '@angular/common';
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
        FilterTransactionWizardPopupModule
    ],
    exports: [
        TargetListContainerComponent
    ],
    providers: []
})
export class TargetListModule { }
