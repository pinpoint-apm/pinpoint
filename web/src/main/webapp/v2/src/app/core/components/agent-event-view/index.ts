import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { AgentEventViewContainerComponent } from './agent-event-view-container.component';
import { AgentEventViewComponent } from './agent-event-view.component';
import { AgentEventsDataService } from './agent-events-data.service';
import { ServerErrorPopupModule } from 'app/core/components/server-error-popup';

@NgModule({
    declarations: [
        AgentEventViewComponent,
        AgentEventViewContainerComponent
    ],
    imports: [
        SharedModule,
        ServerErrorPopupModule
    ],
    exports: [
        AgentEventViewContainerComponent
    ],
    providers: [
        AgentEventsDataService
    ]
})
export class AgentEventViewModule { }
