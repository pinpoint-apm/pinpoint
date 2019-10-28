
import { NgModule } from '@angular/core';

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
        SharedModule,
        FilterTransactionWizardPopupModule
    ],
    exports: [
        TargetListContainerComponent
    ],
    providers: []
})
export class TargetListModule { }
