
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { AgentStatContentsContainerComponent } from './agent-stat-contents-container.component';
import { AgentListModule } from 'app/core/components/agent-list';
import { AgentAdminChartModule } from 'app/core/components/agent-admin-chart';

import { AgentListDataService } from './agent-list-data.service';

@NgModule({
    declarations: [
        AgentStatContentsContainerComponent
    ],
    imports: [
        SharedModule,
        AgentListModule,
        AgentAdminChartModule
    ],
    exports: [
        AgentStatContentsContainerComponent
    ],
    providers: [
        AgentListDataService
    ]
})
export class AgentStatContentsModule { }
