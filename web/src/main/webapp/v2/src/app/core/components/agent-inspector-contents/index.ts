import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { AgentInspectorContentsContainerComponent } from './agent-inspector-contents-container.component';
import { TimelineCommandGroupModule } from 'app/core/components/timeline-command-group';
import { AgentEventViewModule } from 'app/core/components/agent-event-view';
import { AgentInfoModule } from 'app/core/components/agent-info';
import { TimelineModule } from 'app/core/components/timeline';
import { InspectorChartModule } from 'app/core/components/inspector-chart';
import { ChartLayoutOptionModule } from 'app/core/components/chart-layout-option';

@NgModule({
    declarations: [
        AgentInspectorContentsContainerComponent
    ],
    imports: [
        SharedModule,
        TimelineCommandGroupModule,
        AgentEventViewModule,
        AgentInfoModule,
        TimelineModule,
        InspectorChartModule,
        ChartLayoutOptionModule
    ],
    exports: [
        AgentInspectorContentsContainerComponent
    ],
    providers: []
})
export class AgentInspectorContentsModule { }
