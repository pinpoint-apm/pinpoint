import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ResponseSummaryChartComponent } from './response-summary-chart.component';
import { ResponseSummaryChartForSideBarContainerComponent } from './response-summary-chart-for-side-bar-container.component';
import { ResponseSummaryChartForInfoPerServerContainerComponent } from './response-summary-chart-for-info-per-server-container.component';
import { ResponseSummaryChartForFilteredMapSideBarContainerComponent } from './response-summary-chart-for-filtered-map-side-bar-container.component';
import { HelpViewerPopupModule } from 'app/core/components/help-viewer-popup';

@NgModule({
    declarations: [
        ResponseSummaryChartComponent,
        ResponseSummaryChartForSideBarContainerComponent,
        ResponseSummaryChartForFilteredMapSideBarContainerComponent,
        ResponseSummaryChartForInfoPerServerContainerComponent
    ],
    imports: [
        SharedModule,
        HelpViewerPopupModule
    ],
    exports: [
        ResponseSummaryChartForSideBarContainerComponent,
        ResponseSummaryChartForFilteredMapSideBarContainerComponent,
        ResponseSummaryChartForInfoPerServerContainerComponent
    ],
    providers: []
})
export class ResponseSummaryChartModule { }
