import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ChartLayoutOptionContainerComponent } from './chart-layout-option-container.component';
import { ChartLayoutOptionComponent } from './chart-layout-option.component';

@NgModule({
    imports: [
        SharedModule
    ],
    exports: [
        ChartLayoutOptionContainerComponent
    ],
    declarations: [
        ChartLayoutOptionContainerComponent,
        ChartLayoutOptionComponent
    ],
    providers: [],
})
export class ChartLayoutOptionModule { }
