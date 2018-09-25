
import { NgModule } from '@angular/core';
import { AgGridModule } from 'ag-grid-angular/main';
import { SharedModule } from 'app/shared';
import { AgentListContainerComponent } from './agent-list-container.component';
import { AgentListComponent } from './agent-list.component';

@NgModule({
    declarations: [
        AgentListComponent,
        AgentListContainerComponent
    ],
    imports: [
        SharedModule,
        AgGridModule.withComponents([])
    ],
    exports: [
        AgentListContainerComponent
    ],
    providers: []
})
export class AgentListModule { }
