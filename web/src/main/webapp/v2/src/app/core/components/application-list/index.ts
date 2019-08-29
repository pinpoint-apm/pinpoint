
import { NgModule } from '@angular/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ScrollingModule } from '@angular/cdk/scrolling';

import { SharedModule } from 'app/shared';
import { ApplicationListForHeaderContainerComponent } from './application-list-for-header-container.component';
import { ApplicationListForHeaderComponent } from './application-list-for-header.component';
import { ApplicationListForConfigurationContainerComponent } from './application-list-for-configuration-container.component';
import { ApplicationListForConfigurationComponent } from './application-list-for-configuration.component';
import { FavoriteApplicationListForConfigurationContainerComponent } from './favorite-application-list-for-configuration-container.component';
import { FavoriteApplicationListForConfigurationComponent } from './favorite-application-list-for-configuration.component';
import { ApplicationListForConfigurationAlarmContainerComponent } from './application-list-for-configuration-alarm-container.component';
import { ApplicationListInteractionForConfigurationService } from './application-list-interaction-for-configuration.service';

@NgModule({
    declarations: [
        ApplicationListForHeaderContainerComponent,
        ApplicationListForHeaderComponent,
        ApplicationListForConfigurationContainerComponent,
        ApplicationListForConfigurationComponent,
        FavoriteApplicationListForConfigurationComponent,
        FavoriteApplicationListForConfigurationContainerComponent,
        ApplicationListForConfigurationAlarmContainerComponent
    ],
    imports: [
        MatTooltipModule,
        ScrollingModule,
        SharedModule
    ],
    exports: [
        ApplicationListForHeaderContainerComponent,
        ApplicationListForConfigurationContainerComponent,
        FavoriteApplicationListForConfigurationContainerComponent,
        ApplicationListForConfigurationAlarmContainerComponent
    ],
    providers: [
        ApplicationListInteractionForConfigurationService,
    ]
})
export class ApplicationListModule { }
