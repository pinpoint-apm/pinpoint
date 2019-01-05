
import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ConfigurationPopupModule } from 'app/core/components/configuration-popup';
import { ConfigurationIconContainerComponent } from './configuration-icon-container.component';
import { ConfigurationIconComponent } from './configuration-icon.component';

@NgModule({
    declarations: [
        ConfigurationIconComponent,
        ConfigurationIconContainerComponent
    ],
    imports: [
        SharedModule,
        ConfigurationPopupModule
    ],
    exports: [
        ConfigurationIconContainerComponent
    ],
    providers: []
})
export class ConfigurationIconModule { }
