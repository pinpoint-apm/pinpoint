import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ApplicationListModule } from 'app/core/components/application-list';
import { RemovableAgentListModule } from 'app/core/components/removable-agent-list';
import { ConfigurationAgentManagementContainerComponent } from './configuration-agent-management-container.component';

@NgModule({
    declarations: [
        ConfigurationAgentManagementContainerComponent
    ],
    imports: [
        SharedModule,
        ApplicationListModule,
        RemovableAgentListModule
    ],
    exports: [
        ConfigurationAgentManagementContainerComponent
    ],
    providers: []
})
export class ConfigurationAgentManagementModule {}
