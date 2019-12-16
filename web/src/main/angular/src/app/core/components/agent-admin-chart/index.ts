
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { AgentAdminChartContainerComponent } from './agent-admin-chart-container.component';
import { AgentAdminChartComponent } from './agent-admin-chart.component';

@NgModule({
    declarations: [
        AgentAdminChartComponent,
        AgentAdminChartContainerComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [
        AgentAdminChartContainerComponent
    ],
    providers: []
})
export class AgentAdminChartModule { }
