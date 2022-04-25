import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { MetricPageRoutingModule } from './metric-page.routing';
import { MetricPageComponent } from './metric-page.component';
import { ConfigurationIconModule } from 'app/core/components/configuration-icon';
import { HelpViewerPopupModule } from 'app/core/components/help-viewer-popup';
import { NoticeModule } from 'app/core/components/notice';
import { PeriodSelectorModule } from 'app/core/components/period-selector';
import { HostGroupAndHostListModule } from 'app/core/components/host-group-and-host-list';
import { HostGroupListModule } from 'app/core/components/host-group-list';
import { MetricContentsModule } from 'app/core/components/metric-contents';
import { SideNavigationBarModule } from 'app/core/components/side-navigation-bar';

@NgModule({
    declarations: [
        MetricPageComponent
    ],
    imports: [
        SharedModule,
        SideNavigationBarModule,
        NoticeModule,
        HostGroupListModule,
        PeriodSelectorModule,
        ConfigurationIconModule,
        HelpViewerPopupModule,
        HostGroupAndHostListModule,
        MetricContentsModule,
        MetricPageRoutingModule
    ],
    exports: [],
    providers: []
})
export class MetricPageModule {}
