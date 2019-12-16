
import { NgModule } from '@angular/core';
import { ScrollingModule } from '@angular/cdk/scrolling';

import { SharedModule } from 'app/shared';
import { InspectorChartListComponent } from './inspector-chart-list.component';
import { InspectorChartListContainerComponent } from './inspector-chart-list-container.component';
import { InspectorChartListDataService } from './inspector-chart-list-data.service';

@NgModule({
    declarations: [
        InspectorChartListComponent,
        InspectorChartListContainerComponent
    ],
    imports: [
        ScrollingModule,
        SharedModule
    ],
    exports: [
        InspectorChartListContainerComponent
    ],
    providers: [
        InspectorChartListDataService
    ]
})
export class InspectorChartListModule { }
