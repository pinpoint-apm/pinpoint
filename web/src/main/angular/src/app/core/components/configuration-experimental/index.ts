import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ConfigurationExperimentalContainerComponent } from './configuration-experimental-container.component';

@NgModule({
    declarations: [
        ConfigurationExperimentalContainerComponent
    ],
    imports: [
        SharedModule,
    ],
    exports: [
        ConfigurationExperimentalContainerComponent
    ],
    providers: []
})
export class ConfigurationExperimentalModule { }
