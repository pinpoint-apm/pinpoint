
import { NgModule } from '@angular/core';
import { ScrollingModule } from '@angular/cdk/scrolling';

import { SharedModule } from 'app/shared';
import { ApplicationListForHeaderContainerComponent } from './application-list-for-header-container.component';
import { ApplicationListForHeaderComponent } from './application-list-for-header.component';
import { ApplicationListForConfigurationContainerComponent } from './application-list-for-configuration-container.component';
import { ApplicationListForConfigurationComponent } from './application-list-for-configuration.component';
import { FavoriteApplicationListForConfigurationContainerComponent } from './favorite-application-list-for-configuration-container.component';
import { ApplicationListForConfigurationAlarmContainerComponent } from './application-list-for-configuration-alarm-container.component';
import { ApplicationListInteractionForConfigurationService } from './application-list-interaction-for-configuration.service';
import { ApplicationListForAgentManagementContainerComponent } from './application-list-for-agent-management-container.component';

@NgModule({
    declarations: [
        ApplicationListForHeaderContainerComponent,
        ApplicationListForHeaderComponent,
        ApplicationListForConfigurationContainerComponent,
        ApplicationListForConfigurationComponent,
        FavoriteApplicationListForConfigurationContainerComponent,
        ApplicationListForConfigurationAlarmContainerComponent,
        ApplicationListForAgentManagementContainerComponent
    ],
    imports: [
        ScrollingModule,
        SharedModule
    ],
    exports: [
        ApplicationListForHeaderContainerComponent,
        ApplicationListForConfigurationContainerComponent,
        FavoriteApplicationListForConfigurationContainerComponent,
        ApplicationListForConfigurationAlarmContainerComponent,
        ApplicationListForAgentManagementContainerComponent
    ],
    providers: [
        ApplicationListInteractionForConfigurationService,
    ]
})
export class ApplicationListModule { }
