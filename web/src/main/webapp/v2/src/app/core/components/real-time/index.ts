import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { RealTimeChartComponent } from './real-time-chart.component';
import { RealTimeContainerComponent } from './real-time-container.component';
import { RealTimePagingContainerComponent } from './real-time-paging-container.component';
import { RealTimeTotalChartComponent } from './real-time-total-chart.component';
import { RealTimeAgentChartComponent } from './real-time-agent-chart.component';
import { RealTimeWebSocketService } from './real-time-websocket.service';
import { ResizeTopDirective } from './resize-top.directive';
import { HelpViewerPopupModule } from 'app/core/components/help-viewer-popup';

@NgModule({
    declarations: [
        ResizeTopDirective,
        RealTimeChartComponent,
        RealTimeAgentChartComponent,
        RealTimeTotalChartComponent,
        RealTimeContainerComponent,
        RealTimePagingContainerComponent
    ],
    imports: [
        SharedModule,
        HelpViewerPopupModule
    ],
    exports: [
        RealTimeContainerComponent,
        RealTimePagingContainerComponent
    ],
    entryComponents: [
        RealTimeTotalChartComponent,
        RealTimeAgentChartComponent
    ],
    providers: [
        RealTimeWebSocketService
    ]
})
export class RealTimeModule { }
