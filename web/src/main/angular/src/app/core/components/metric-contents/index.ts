import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ChartLayoutOptionModule } from 'app/core/components/chart-layout-option';
import { MetricContentsContainerComponent } from './metric-contents-container.component';
import { MetricModule } from 'app/core/components/metric';

@NgModule({
    declarations: [
        MetricContentsContainerComponent
    ],
    imports: [
        SharedModule,
        ChartLayoutOptionModule,
        MetricModule
    ],
    exports: [
        MetricContentsContainerComponent
    ],
    providers: [],
})
export class MetricContentsModule { }
