import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';

import { UrlStatisticContainerComponent } from './url-statistic-container.component';
import { UrlStatisticChartComponent } from './url-statistic-chart.component';
import { UrlStatisticDataService } from './url-statistic-data.service';
import { UrlStatisticInfoComponent } from './url-statistic-info.component';

@NgModule({
    imports: [
        SharedModule
    ],
    exports: [
        UrlStatisticContainerComponent
    ],
    declarations: [
        UrlStatisticContainerComponent,
        UrlStatisticChartComponent,
        UrlStatisticInfoComponent
    ],
    providers: [
        UrlStatisticDataService
    ],
})
export class UrlStatisticModule { }
