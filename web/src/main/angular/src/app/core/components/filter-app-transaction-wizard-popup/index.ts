
import { NgModule } from '@angular/core';
import { NouisliderModule } from 'ng2-nouislider';

import { SharedModule } from 'app/shared';
import { FilterAppTransactionWizardPopupComponent } from './filter-app-transaction-wizard-popup.component';
import { FilterAppTransactionWizardPopupContainerComponent } from './filter-app-transaction-wizard-popup-container.component';

@NgModule({
    declarations: [
        FilterAppTransactionWizardPopupComponent,
        FilterAppTransactionWizardPopupContainerComponent
    ],
    imports: [
        NouisliderModule,
        SharedModule
    ],
    exports: [],
    entryComponents: [
        FilterAppTransactionWizardPopupContainerComponent
    ],
    providers: []
})
export class FilterAppTransactionWizardPopupModule { }
