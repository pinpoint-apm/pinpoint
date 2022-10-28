import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { UrlStatisticInfoContainerComponent } from './url-statistic-info-container.component';
import { UrlStatisticInfoDataService } from './url-statistic-info-data.service';
import { UrlStatisticInfoComponent } from './url-statistic-info.component';

@NgModule({
    imports: [
        SharedModule
    ],
    exports: [
        UrlStatisticInfoContainerComponent
    ],
    declarations: [
        UrlStatisticInfoContainerComponent,
        UrlStatisticInfoComponent
    ],
    providers: [
        UrlStatisticInfoDataService
    ],
})
export class UrlStatisticInfoModule { }
