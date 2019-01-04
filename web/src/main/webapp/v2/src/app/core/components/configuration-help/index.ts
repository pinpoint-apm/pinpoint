import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ConfigurationHelpContainerComponent } from './configuration-help-container.component';

@NgModule({
    declarations: [
        ConfigurationHelpContainerComponent
    ],
    imports: [
        SharedModule,
    ],
    exports: [
        ConfigurationHelpContainerComponent
    ],
    providers: []
})
export class ConfigurationHelpModule { }
