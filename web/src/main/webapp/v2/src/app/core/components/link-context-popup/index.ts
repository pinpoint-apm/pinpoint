import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { LinkContextPopupContainerComponent } from 'app/core/components/link-context-popup/link-context-popup-container.component';
import { LinkContextPopupComponent } from 'app/core/components/link-context-popup/link-context-popup.component';
import { FilterTransactionWizardPopupModule } from 'app/core/components/filter-transaction-wizard-popup';

@NgModule({
    declarations: [
        LinkContextPopupContainerComponent,
        LinkContextPopupComponent
    ],
    imports: [
        SharedModule,
        FilterTransactionWizardPopupModule
    ],
    exports: [],
    entryComponents: [
        LinkContextPopupContainerComponent
    ],
    providers: []
})
export class LinkContextPopupModule { }
