import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { AgentStatisticChartContainerComponent } from './agent-statistic-chart-container.component';
import { AgentStatisticChartComponent } from './agent-statistic-chart.component';

@NgModule({
    imports: [
        SharedModule
    ],
    exports: [
        AgentStatisticChartContainerComponent
    ],
    declarations: [
        AgentStatisticChartContainerComponent,
        AgentStatisticChartComponent
    ],
    providers: [],
})
export class AgentStatisticChartModule { }
