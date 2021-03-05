import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { UrlStatisticPageRoutingModule } from './url-statistic-page.routing';
import { UrlStatisticPageComponent } from './url-statistic-page.component';
import { UrlStatisticModule } from 'app/core/components/url-statistic';

@NgModule({
    declarations: [
        UrlStatisticPageComponent
    ],
    imports: [
        SharedModule,
        UrlStatisticPageRoutingModule,
        UrlStatisticModule
    ],
    exports: [],
    providers: []
})
export class UrlStatisticPageModule {}
