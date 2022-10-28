import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { UrlStatisticChartModule } from 'app/core/components/url-statistic-chart';
import { UrlStatisticInfoModule } from 'app/core/components/url-statistic-info';
import { UrlStatisticContentsContainerComponent } from './url-statistic-contents-container.component';

@NgModule({
    declarations: [
        UrlStatisticContentsContainerComponent
    ],
    imports: [
        SharedModule,
        UrlStatisticInfoModule,
        UrlStatisticChartModule
    ],
    exports: [
        UrlStatisticContentsContainerComponent
    ],
    providers: [],
})
export class UrlStatisticContentsModule { }
