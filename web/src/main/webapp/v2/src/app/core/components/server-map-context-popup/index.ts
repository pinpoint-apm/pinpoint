import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ServerMapContextPopupContainerComponent } from 'app/core/components/server-map-context-popup/server-map-context-popup-container.component';
import { ServerMapContextPopupComponent } from 'app/core/components/server-map-context-popup/server-map-context-popup.component';

@NgModule({
    declarations: [
        ServerMapContextPopupContainerComponent,
        ServerMapContextPopupComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [],
    entryComponents: [
        ServerMapContextPopupContainerComponent
    ],
    providers: [],
})
export class ServerMapContextPopupModule { }
