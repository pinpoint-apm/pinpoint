import { NgModule } from '@angular/core';
import { AgGridModule } from 'ag-grid-angular';

import { SharedModule } from 'app/shared';
import { AgentStatisticListComponent } from './agent-statistic-list.component';
import { AgentStatisticListContainerComponent } from './agent-statistic-list-container.component';

@NgModule({
    declarations: [
        AgentStatisticListComponent,
        AgentStatisticListContainerComponent
    ],
    imports: [
        SharedModule,
        AgGridModule.withComponents([])
    ],
    exports: [
        AgentStatisticListContainerComponent
    ],
    providers: [
    ]
})
export class AgentStatisticListModule { }
