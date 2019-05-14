import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ConfigPageComponent } from './config-page.component';
import { ConfigPageRoutingModule } from './config-page.routing';
import { ConfigurationGeneralModule } from 'app/core/components/configuration-general';
import { ConfigurationFavoriteModule } from 'app/core/components/configuration-favorite';
import { ConfigurationChartLayoutModule } from 'app/core/components/configuration-chart-layout';
import { ConfigurationUserGroupModule } from 'app/core/components/configuration-user-group';
import { ConfigurationInstallationModule } from 'app/core/components/configuration-installation';
import { ConfigurationHelpModule } from 'app/core/components/configuration-help';
import { ConfigurationAlarmModule } from 'app/core/components/configuration-alarm';

@NgModule({
    declarations: [
        ConfigPageComponent
    ],
    imports: [
        SharedModule,
        ConfigPageRoutingModule,
        ConfigurationGeneralModule,
        ConfigurationFavoriteModule,
        ConfigurationChartLayoutModule,
        ConfigurationUserGroupModule,
        ConfigurationAlarmModule,
        ConfigurationInstallationModule,
        ConfigurationHelpModule
    ],
    exports: [],
    providers: []
})
export class ConfigPageModule {}
