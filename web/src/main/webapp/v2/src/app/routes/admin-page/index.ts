import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { SharedModule } from 'app/shared';
import { AdminPageComponent } from './admin-page.component';
import { routing } from './admin-page.routing';

import { AgentManagementContentsModule } from 'app/core/components/agent-management-contents';
import { AgentStatContentsModule } from 'app/core/components/agent-stat-contents';

@NgModule({
    declarations: [
        AdminPageComponent
    ],
    imports: [
        RouterModule.forChild(routing),
        SharedModule,
        AgentStatContentsModule,
        AgentManagementContentsModule
    ],
    exports: [],
    providers: []
})
export class AdminPageModule { }
