
import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ServerErrorPopupComponent } from './server-error-popup.component';
import { ServerErrorPopupContainerComponent } from './server-error-popup-container.component';

@NgModule({
    declarations: [
        ServerErrorPopupComponent,
        ServerErrorPopupContainerComponent
    ],
    imports: [
        SharedModule
    ],
    entryComponents: [
        ServerErrorPopupContainerComponent
    ],
    providers: []
})
export class ServerErrorPopupModule {}
