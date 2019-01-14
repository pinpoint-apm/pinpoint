
import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { LoadChartComponent } from './load-chart.component';
import { LoadChartForSideBarContainerComponent } from './load-chart-for-side-bar-container.component';
import { LoadChartForInfoPerServerContainerComponent } from './load-chart-for-info-per-server-container.component';
import { LoadChartForFilteredMapSideBarContainerComponent } from './load-chart-for-filtered-map-side-bar-container.component';
import { HelpViewerPopupModule } from 'app/core/components/help-viewer-popup';

@NgModule({
    declarations: [
        LoadChartComponent,
        LoadChartForSideBarContainerComponent,
        LoadChartForFilteredMapSideBarContainerComponent,
        LoadChartForInfoPerServerContainerComponent
    ],
    imports: [
        SharedModule,
        HelpViewerPopupModule
    ],
    exports: [
        LoadChartForSideBarContainerComponent,
        LoadChartForFilteredMapSideBarContainerComponent,
        LoadChartForInfoPerServerContainerComponent
    ],
    providers: []
})
export class LoadChartModule { }
