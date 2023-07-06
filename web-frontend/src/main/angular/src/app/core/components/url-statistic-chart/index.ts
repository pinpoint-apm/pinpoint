import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { UrlStatisticChartContainerComponent } from './url-statistic-chart-container.component';
import { UrlStatisticChartComponent } from './url-statistic-chart.component';
import { UrlStatisticDefaultChartComponent } from './url-statistic-chart-default-chart.component';
import { UrlStatisticBarChartComponent } from './url-statistic-bar-chart.component';
import { UrlStatisticLineChartComponent } from './url-statistic-line-chart.component';

@NgModule({
    imports: [
        SharedModule
    ],
    exports: [
        UrlStatisticChartContainerComponent
    ],
    declarations: [
        UrlStatisticChartContainerComponent,
        UrlStatisticChartComponent,
        UrlStatisticDefaultChartComponent,
        UrlStatisticBarChartComponent,
        UrlStatisticLineChartComponent
    ],
    providers: [],
    entryComponents: [
        UrlStatisticBarChartComponent,
        UrlStatisticLineChartComponent
    ]
})
export class UrlStatisticChartModule { }
