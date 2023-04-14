import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { UrlStatisticPageRoutingModule } from './url-statistic-page.routing';
import { UrlStatisticPageComponent } from './url-statistic-page.component';
import { ApplicationListModule } from 'app/core/components/application-list';
import { NoticeModule } from 'app/core/components/notice';
import { PeriodSelectorModule } from 'app/core/components/period-selector';
import { ServerAndAgentListModule } from 'app/core/components/server-and-agent-list';
import { SideNavigationBarModule } from 'app/core/components/side-navigation-bar';
import { UrlStatisticContentsModule } from 'app/core/components/url-statistic-contents';
import { HelpViewerPopupModule } from 'app/core/components/help-viewer-popup';

@NgModule({
    declarations: [
        UrlStatisticPageComponent
    ],
    imports: [
        SharedModule,
        NoticeModule,
        SideNavigationBarModule,
        ApplicationListModule,
        PeriodSelectorModule,
        HelpViewerPopupModule,
        ServerAndAgentListModule,
        UrlStatisticContentsModule,
        UrlStatisticPageRoutingModule,
    ],
    exports: [],
    providers: []
})
export class UrlStatisticPageModule {}
