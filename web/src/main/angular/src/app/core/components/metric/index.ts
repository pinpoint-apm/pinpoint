import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { MetricContainerComponent } from './metric-container.component';
import { MetricDataService } from './metric-data.service';
import { MetricComponent } from './metric.component';

@NgModule({
    declarations: [
        MetricContainerComponent,
        MetricComponent
    ],
    imports: [
        SharedModule,
    ],
    exports: [
        MetricContainerComponent
    ],
    providers: [
        MetricDataService
    ],
})
export class MetricModule { }
