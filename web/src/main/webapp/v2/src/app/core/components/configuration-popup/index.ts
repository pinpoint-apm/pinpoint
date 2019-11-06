
import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ConfigurationPopupContainerComponent } from './configuration-popup-container.component';
import { ConfigurationPopupComponent } from './configuration-popup.component';

@NgModule({
    declarations: [
        ConfigurationPopupContainerComponent,
        ConfigurationPopupComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [],
    entryComponents: [
        ConfigurationPopupContainerComponent,
    ],
    providers: []
})
export class ConfigurationPopupModule { }
