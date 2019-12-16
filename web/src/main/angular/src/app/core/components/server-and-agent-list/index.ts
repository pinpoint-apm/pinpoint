import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ServerAndAgentListComponent } from './server-and-agent-list.component';
import { ServerAndAgentListContainerComponent } from './server-and-agent-list-container.component';
import { ServerErrorPopupModule } from 'app/core/components/server-error-popup';
import { ServerAndAgentListDataService } from './server-and-agent-list-data.service';

@NgModule({
    declarations: [
        ServerAndAgentListComponent,
        ServerAndAgentListContainerComponent
    ],
    imports: [
        SharedModule,
        ServerErrorPopupModule,
    ],
    exports: [
        ServerAndAgentListContainerComponent
    ],
    providers: [
        ServerAndAgentListDataService
    ]
})
export class ServerAndAgentListModule { }
