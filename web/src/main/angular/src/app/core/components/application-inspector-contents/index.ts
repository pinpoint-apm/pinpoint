import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ApplicationInspectorUsageGuideModule } from 'app/core/components/application-inspector-usage-guide';
import { ApplicationInspectorContentsContainerComponent } from './application-inspector-contents-container.component';
import { TimelineCommandGroupModule } from 'app/core/components/timeline-command-group';
import { AgentEventViewModule } from 'app/core/components/agent-event-view';
import { TimelineModule } from 'app/core/components/timeline';
import { InspectorChartModule } from 'app/core/components/inspector-chart';
import { ChartLayoutOptionModule } from 'app/core/components/chart-layout-option';

@NgModule({
    declarations: [
        ApplicationInspectorContentsContainerComponent
    ],
imports: [
        SharedModule,
        TimelineCommandGroupModule,
        AgentEventViewModule,
        TimelineModule,
        InspectorChartModule,
        ApplicationInspectorUsageGuideModule,
        ChartLayoutOptionModule
    ],
    exports: [
        ApplicationInspectorContentsContainerComponent
    ],
    providers: []
})
export class ApplicationInspectorContentsModule { }
