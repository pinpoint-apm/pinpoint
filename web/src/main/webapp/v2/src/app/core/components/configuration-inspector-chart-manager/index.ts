import { NgModule } from '@angular/core';

import { MatTabsModule } from '@angular/material/tabs';
import { SharedModule } from 'app/shared';
import { ChartLayoutModule } from 'app/core/components/chart-layout';
import { InspectorChartListModule } from 'app/core/components/inspector-chart-list';
import { ConfigurationInspectorChartManagerContainerComponent } from './configuration-inspector-chart-manager-container.component';

@NgModule({
    declarations: [
        ConfigurationInspectorChartManagerContainerComponent
    ],
    imports: [
        MatTabsModule,
        SharedModule,
        ChartLayoutModule,
        InspectorChartListModule
    ],
    exports: [
        ConfigurationInspectorChartManagerContainerComponent
    ],
    providers: []
})
export class ConfigurationInspectorChartManagerModule { }
