
import { NgModule } from '@angular/core';
import { NouisliderModule } from 'ng2-nouislider';

import { SharedModule } from 'app/shared';
import { FilterTransactionWizardPopupComponent } from './filter-transaction-wizard-popup.component';
import { FilterTransactionWizardPopupContainerComponent } from './filter-transaction-wizard-popup-container.component';

@NgModule({
    declarations: [
        FilterTransactionWizardPopupComponent,
        FilterTransactionWizardPopupContainerComponent
    ],
    imports: [
        NouisliderModule,
        SharedModule
    ],
    exports: [],
    entryComponents: [
        FilterTransactionWizardPopupContainerComponent
    ],
    providers: []
})
export class FilterTransactionWizardPopupModule { }
