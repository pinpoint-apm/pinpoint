import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { HelpViewerPopupModule } from 'app/core/components/help-viewer-popup';
import { ResizeTopDirective } from 'app/core/components/real-time/resize-top.directive';
import { RealTimeAgentChartComponent } from 'app/core/components/real-time/real-time-agent-chart.component';
import { RealTimeChartComponent } from 'app/core/components/real-time/real-time-chart.component';
import { RealTimeContainerComponent } from 'app/core/components/real-time/real-time-container.component';
import { RealTimeTotalChartComponent } from 'app/core/components/real-time/real-time-total-chart.component';
import { RealTimePagingContainerComponent } from 'app/core/components/real-time/real-time-paging-container.component';
import { RealTimeWebSocketService } from 'app/core/components/real-time/real-time-websocket.service';

@NgModule({
    declarations: [
        ResizeTopDirective,
        RealTimeAgentChartComponent,
        RealTimeChartComponent,
        RealTimeContainerComponent,
        RealTimeTotalChartComponent,
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
    providers: [
        RealTimeWebSocketService
    ]
})
export class RealTimeModule { }
