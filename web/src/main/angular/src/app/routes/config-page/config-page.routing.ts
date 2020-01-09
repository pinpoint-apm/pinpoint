import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { ConfigPageComponent } from './config-page.component';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { ConfigurationGeneralContainerComponent } from 'app/core/components/configuration-general/configuration-general-container.component';
import { ConfigurationFavoriteContainerComponent } from 'app/core/components/configuration-favorite/configuration-favorite-container.component';
import { ConfigurationInspectorChartManagerContainerComponent } from 'app/core/components/configuration-inspector-chart-manager/configuration-inspector-chart-manager-container.component';
import { ConfigurationUserGroupContainerComponent } from 'app/core/components/configuration-user-group/configuration-user-group-container.component';
import { ConfigurationAlarmContainerComponent } from 'app/core/components/configuration-alarm/configuration-alarm-container.component';
import { ConfigurationInstallationContainerComponent } from 'app/core/components/configuration-installation/configuration-installation-container.component';
import { ConfigurationHelpContainerComponent } from 'app/core/components/configuration-help/configuration-help-container.component';
import { ConfigurationAgentStatisticContainerComponent } from 'app/core/components/configuration-agent-statistic/configuration-agent-statistic-container.component';
import { ConfigurationAgentManagementContainerComponent } from 'app/core/components/configuration-agent-management/configuration-agent-management-container.component';

const routes: Routes = [
    {
        path: '',
        component: ConfigPageComponent,
        children: [
            {
                path: UrlPathId.AGENT_STATISTIC,
                component: ConfigurationAgentStatisticContainerComponent
            },
            {
                path: UrlPathId.AGENT_MANAGEMENT,
                component: ConfigurationAgentManagementContainerComponent
            },
            {
                path: UrlPathId.GENERAL,
                component: ConfigurationGeneralContainerComponent
            },
            {
                path: UrlPathId.FAVORITE,
                component: ConfigurationFavoriteContainerComponent
            },
            {
                path: UrlPathId.CHART_MANAGER,
                component: ConfigurationInspectorChartManagerContainerComponent
            },
            {
                path: UrlPathId.USER_GROUP,
                component: ConfigurationUserGroupContainerComponent
            },
            {
                path: UrlPathId.ALARM,
                component: ConfigurationAlarmContainerComponent
            },
            {
                path: UrlPathId.INSTALLATION,
                component: ConfigurationInstallationContainerComponent
            },
            {
                path: UrlPathId.HELP,
                component: ConfigurationHelpContainerComponent
            },
            {
                path: '',
                redirectTo: `/${UrlPath.CONFIG}/${UrlPathId.GENERAL}`,
                pathMatch: 'full'
            }
        ]
    }
];

@NgModule({
    imports: [
        RouterModule.forChild(routes)
    ],
    exports: [
        RouterModule
    ]
})
export class ConfigPageRoutingModule {}
