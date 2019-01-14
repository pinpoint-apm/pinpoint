
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TimelineComponent } from './timeline.component';
import { TimelineInteractionService } from './timeline-interaction.service';
import { AgentInspectorTimelineContainerComponent } from './agent-inspector-timeline-container.component';
import { ApplicationInspectorTimelineContainerComponent } from './application-inspector-timeline-container.component';
import { AgentTimelineDataService } from './agent-timeline-data.service';

@NgModule({
    declarations: [
        TimelineComponent,
        AgentInspectorTimelineContainerComponent,
        ApplicationInspectorTimelineContainerComponent
    ],
    imports: [
        CommonModule
    ],
    exports: [
        AgentInspectorTimelineContainerComponent,
        ApplicationInspectorTimelineContainerComponent
    ],
    providers: [
        TimelineInteractionService,
        AgentTimelineDataService
    ]
})
export class TimelineModule { }
