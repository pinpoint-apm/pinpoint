
import { NgModule } from '@angular/core';
import { AgGridModule } from 'ag-grid-angular';

import { SharedModule } from 'app/shared';
import { RemovableAgentListComponent } from './removable-agent-list.component';
import { RemovableAgentListContainerComponent } from './removable-agent-list-container.component';
import { RemovableAgentDataService } from './removable-agent-data.service';

@NgModule({
    declarations: [
        RemovableAgentListComponent,
        RemovableAgentListContainerComponent
    ],
    imports: [
        SharedModule,
        AgGridModule.withComponents([])
    ],
    exports: [
        RemovableAgentListContainerComponent
    ],
    providers: [
        RemovableAgentDataService
    ]
})
export class RemovableAgentListModule { }
