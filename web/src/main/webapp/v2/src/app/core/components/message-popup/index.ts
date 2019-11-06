
import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { MessagePopupContainerComponent } from './message-popup-container.component';
import { MessagePopupComponent } from 'app/core/components/message-popup/message-popup.component';

@NgModule({
    declarations: [
        MessagePopupContainerComponent,
        MessagePopupComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [],
    entryComponents: [
        MessagePopupContainerComponent
    ],
    providers: []
})
export class MessagePopupModule { }
