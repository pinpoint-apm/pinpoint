import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ChartLayoutModule } from 'app/core/components/chart-layout';
import { ConfigurationChartLayoutContainerComponent } from './configuration-chart-layout-container.component';

@NgModule({
    declarations: [
        ConfigurationChartLayoutContainerComponent
    ],
    imports: [
        SharedModule,
        ChartLayoutModule
    ],
    exports: [
        ConfigurationChartLayoutContainerComponent
    ],
    providers: []
})
export class ConfigurationChartLayoutModule { }
