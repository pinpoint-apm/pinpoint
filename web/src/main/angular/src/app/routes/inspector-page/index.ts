import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { routing } from './inspector-page.routing';
import { SharedModule } from 'app/shared';
import { NoticeModule } from 'app/core/components/notice';
import { ApplicationListModule } from 'app/core/components/application-list';
import { PeriodSelectorModule } from 'app/core/components/period-selector';
import { ConfigurationIconModule } from 'app/core/components/configuration-icon';
import { ApplicationInspectorTitleModule } from 'app/core/components/application-inspector-title';
import { ServerAndAgentListModule } from 'app/core/components/server-and-agent-list';
import { AgentSearchInputModule } from 'app/core/components/agent-search-input';
import { ApplicationInspectorContentsModule } from 'app/core/components/application-inspector-contents';
import { AgentInspectorContentsModule } from 'app/core/components/agent-inspector-contents';
import { InspectorPageComponent } from './inspector-page.component';
import { HelpViewerPopupModule } from 'app/core/components/help-viewer-popup';

@NgModule({
    declarations: [
        InspectorPageComponent
    ],
    imports: [
        SharedModule,
        NoticeModule,
        ApplicationListModule,
        PeriodSelectorModule,
        ConfigurationIconModule,
        ApplicationInspectorTitleModule,
        ServerAndAgentListModule,
        ApplicationInspectorContentsModule,
        AgentInspectorContentsModule,
        AgentSearchInputModule,
        HelpViewerPopupModule,
        RouterModule.forChild(routing)
    ],
    exports: [],
    providers: []
})
export class InspectorPageModule {}
