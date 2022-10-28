import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { UrlStatisticChartContainerComponent } from './url-statistic-chart-container.component';
import { UrlStatisticChartDataService } from './url-statistic-chart-data.service';
import { UrlStatisticChartComponent } from './url-statistic-chart.component';

@NgModule({
    imports: [
        SharedModule
    ],
    exports: [
        UrlStatisticChartContainerComponent
    ],
    declarations: [
        UrlStatisticChartContainerComponent,
        UrlStatisticChartComponent
    ],
    providers: [
        UrlStatisticChartDataService
    ],
})
export class UrlStatisticChartModule { }
