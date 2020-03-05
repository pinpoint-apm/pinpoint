import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ApplicationListModule } from 'app/core/components/application-list';
import { ConfigurationFavoriteContainerComponent } from './configuration-favorite-container.component';

@NgModule({
    declarations: [
        ConfigurationFavoriteContainerComponent
    ],
    imports: [
        SharedModule,
        ApplicationListModule
    ],
    exports: [
        ConfigurationFavoriteContainerComponent
    ],
    providers: []
})
export class ConfigurationFavoriteModule { }
