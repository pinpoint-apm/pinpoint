import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { UrlStatisticContentsContainerComponent } from './url-statistic-contents-container.component';

@NgModule({
    declarations: [
        UrlStatisticContentsContainerComponent
    ],
    imports: [
        SharedModule,
    ],
    exports: [
        UrlStatisticContentsContainerComponent
    ],
    providers: [],
})
export class UrlStatisticContentsModule { }
