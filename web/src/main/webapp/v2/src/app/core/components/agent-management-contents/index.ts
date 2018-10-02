
import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { AgentManagementContentsContainerComponent } from './agent-management-contents-container.component';

@NgModule({
    declarations: [
        AgentManagementContentsContainerComponent
    ],
    imports: [
        SharedModule,
    ],
    exports: [
        AgentManagementContentsContainerComponent
    ],
    providers: []
})
export class AgentManagementContentsModule { }
