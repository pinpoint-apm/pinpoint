import { NgModule } from '@angular/core';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { MatGridListModule } from '@angular/material';

import { SharedModule } from 'app/shared';
import { ChartLayoutContainerComponent } from './chart-layout-container.component';
import { ChartLayoutComponent } from './chart-layout.component';

@NgModule({
    imports: [
        SharedModule,
        MatGridListModule,
        DragDropModule
    ],
    exports: [
        ChartLayoutContainerComponent
    ],
    declarations: [
        ChartLayoutContainerComponent,
        ChartLayoutComponent
    ],
    providers: [],
})
export class ChartLayoutModule { }
