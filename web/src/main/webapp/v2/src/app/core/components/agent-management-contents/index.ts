
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { AgentManagerModule } from 'app/core/components/agent-manager';
import { ApplicationListModule } from 'app/core/components/application-list';
import { AgentManagementContentsContainerComponent } from './agent-management-contents-container.component';

@NgModule({
    declarations: [
        AgentManagementContentsContainerComponent
    ],
    imports: [
        SharedModule,
        ApplicationListModule,
        AgentManagerModule
    ],
    exports: [
        AgentManagementContentsContainerComponent
    ],
    providers: []
})
export class AgentManagementContentsModule { }
