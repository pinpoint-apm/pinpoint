
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { AgentEventViewContainerComponent } from './agent-event-view-container.component';
import { AgentEventViewComponent } from './agent-event-view.component';
import { AgentEventsDataService } from './agent-events-data.service';

@NgModule({
    declarations: [
        AgentEventViewComponent,
        AgentEventViewContainerComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [
        AgentEventViewContainerComponent
    ],
    providers: [
        AgentEventsDataService
    ]
})
export class AgentEventViewModule { }
