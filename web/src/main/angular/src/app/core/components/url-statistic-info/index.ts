import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { UrlStatisticInfoContainerComponent } from './url-statistic-info-container.component';
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
    providers: [],
})
export class UrlStatisticInfoModule { }
