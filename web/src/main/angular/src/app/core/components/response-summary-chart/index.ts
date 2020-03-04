import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ResponseSummaryChartComponent } from './response-summary-chart.component';
import { ResponseSummaryChartContainerComponent } from './response-summary-chart-container.component';
import { HelpViewerPopupModule } from 'app/core/components/help-viewer-popup';

@NgModule({
    declarations: [
        ResponseSummaryChartComponent,
        ResponseSummaryChartContainerComponent
    ],
    imports: [
        SharedModule,
        HelpViewerPopupModule
    ],
    exports: [
        ResponseSummaryChartContainerComponent
    ],
    providers: []
})
export class ResponseSummaryChartModule { }
