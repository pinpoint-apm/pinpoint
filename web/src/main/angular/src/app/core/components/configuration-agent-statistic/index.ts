import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { AgentStatisticChartModule } from 'app/core/components/agent-statistic-chart';
import { AgentStatisticListModule } from 'app/core/components/agent-statistic-list';
import { ConfigurationAgentStatisticContainerComponent } from './configuration-agent-statistic-container.component';
import { AgentStatisticDataService } from './agent-statistic-data.service';
import { ServerErrorPopupModule } from 'app/core/components/server-error-popup';

@NgModule({
    declarations: [
        ConfigurationAgentStatisticContainerComponent
    ],
    imports: [
        SharedModule,
        AgentStatisticChartModule,
        AgentStatisticListModule,
        ServerErrorPopupModule
    ],
    exports: [
        ConfigurationAgentStatisticContainerComponent
    ],
    providers: [
        AgentStatisticDataService
    ]
})
export class ConfigurationAgentStatisticModule { }
