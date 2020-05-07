import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { NodeContextPopupContainerComponent } from 'app/core/components/node-context-popup/node-context-popup-container.component';
import { NodeContextPopupComponent } from 'app/core/components/node-context-popup/node-context-popup.component';
import { FilterAppTransactionWizardPopupModule } from 'app/core/components/filter-app-transaction-wizard-popup';

@NgModule({
    declarations: [
        NodeContextPopupContainerComponent,
        NodeContextPopupComponent
    ],
    imports: [
        SharedModule,
        FilterAppTransactionWizardPopupModule
    ],
    exports: [],
    entryComponents: [
        NodeContextPopupContainerComponent
    ],
    providers: []
})
export class NodeContextPopupModule { }
