import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { routing } from './inspector-page.routing';
import { SharedModule } from 'app/shared';
import { NoticeModule } from 'app/core/components/notice';
import { ApplicationListModule } from 'app/core/components/application-list';
import { PeriodSelectorModule } from 'app/core/components/period-selector';
import { ConfigurationIconModule } from 'app/core/components/configuration-icon';
import { ServerAndAgentListModule } from 'app/core/components/server-and-agent-list';
import { ApplicationInspectorContentsModule } from 'app/core/components/application-inspector-contents';
import { AgentInspectorContentsModule } from 'app/core/components/agent-inspector-contents';
import { InspectorPageComponent } from './inspector-page.component';
import { HelpViewerPopupModule } from 'app/core/components/help-viewer-popup';
import { AppWidgetModule } from 'app/core/components/app-widget';
import { SideNavigationBarModule } from 'app/core/components/side-navigation-bar';

@NgModule({
    declarations: [
        InspectorPageComponent
    ],
    imports: [
        SharedModule,
        SideNavigationBarModule,
        NoticeModule,
        ApplicationListModule,
        PeriodSelectorModule,
        ConfigurationIconModule,
        ServerAndAgentListModule,
        ApplicationInspectorContentsModule,
        AgentInspectorContentsModule,
        HelpViewerPopupModule,
        RouterModule.forChild(routing),
        AppWidgetModule,
    ],
    exports: [],
    providers: []
})
export class InspectorPageModule {}
