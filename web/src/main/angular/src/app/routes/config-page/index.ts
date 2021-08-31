import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ConfigPageComponent } from './config-page.component';
import { ConfigPageRoutingModule } from './config-page.routing';
import { ConfigurationGeneralModule } from 'app/core/components/configuration-general';
import { ConfigurationFavoriteModule } from 'app/core/components/configuration-favorite';
import { ConfigurationInspectorChartManagerModule } from 'app/core/components/configuration-inspector-chart-manager';
import { ConfigurationUserGroupModule } from 'app/core/components/configuration-user-group';
import { ConfigurationWebhookModule } from 'app/core/components/configuration-webhook';
import { ConfigurationInstallationModule } from 'app/core/components/configuration-installation';
import { ConfigurationHelpModule } from 'app/core/components/configuration-help';
import { ConfigurationExperimentalModule } from 'app/core/components/configuration-experimental';
import { ConfigurationAlarmModule } from 'app/core/components/configuration-alarm';
import { ConfigurationAgentStatisticModule } from 'app/core/components/configuration-agent-statistic';
import { ConfigurationAgentManagementModule } from 'app/core/components/configuration-agent-management';

@NgModule({
    declarations: [
        ConfigPageComponent
    ],
    imports: [
        SharedModule,
        ConfigPageRoutingModule,
        ConfigurationGeneralModule,
        ConfigurationFavoriteModule,
        ConfigurationInspectorChartManagerModule,
        ConfigurationUserGroupModule,
        ConfigurationAlarmModule,
        ConfigurationWebhookModule,
        ConfigurationInstallationModule,
        ConfigurationHelpModule,
        ConfigurationExperimentalModule,
        ConfigurationAgentStatisticModule,
        ConfigurationAgentManagementModule
    ],
    exports: [],
    providers: []
})
export class ConfigPageModule {}
