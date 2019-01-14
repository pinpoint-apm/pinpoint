
import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { AgentManagementContentsContainerComponent } from './agent-management-contents-container.component';
import { AgentManagerModule } from 'app/core/components/agent-manager';

@NgModule({
    declarations: [
        AgentManagementContentsContainerComponent
    ],
    imports: [
        SharedModule,
        AgentManagerModule
    ],
    exports: [
        AgentManagementContentsContainerComponent
    ],
    providers: []
})
export class AgentManagementContentsModule { }
