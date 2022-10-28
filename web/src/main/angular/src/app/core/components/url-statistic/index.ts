import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';

import { UrlStatisticContainerComponent } from './url-statistic-container.component';
import { UrlStatisticDataService } from './url-statistic-data.service';

@NgModule({
    imports: [
        SharedModule
    ],
    exports: [
        UrlStatisticContainerComponent
    ],
    declarations: [
        UrlStatisticContainerComponent,
    ],
    providers: [
        UrlStatisticDataService
    ],
})
export class UrlStatisticModule { }
