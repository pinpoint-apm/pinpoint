import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { HelpViewerPopupModule } from 'app/core/components/help-viewer-popup';
import { NewResizeTopDirective } from 'app/core/components/real-time-new/new-resize-top.directive';
import { NewRealTimeAgentChartComponent } from 'app/core/components/real-time-new/new-real-time-agent-chart.component';
import { NewRealTimeChartComponent } from 'app/core/components/real-time-new/new-real-time-chart.component';
import { NewRealTimeContainerComponent } from 'app/core/components/real-time-new/new-real-time-container.component';
import { NewRealTimeTotalChartComponent } from 'app/core/components/real-time-new/new-real-time-total-chart.component';
import { NewRealTimePagingContainerComponent } from 'app/core/components/real-time-new/new-real-time-paging-container.component';
import { NewRealTimeWebSocketService } from 'app/core/components/real-time-new/new-real-time-websocket.service';

@NgModule({
    declarations: [
        NewResizeTopDirective,
        NewRealTimeAgentChartComponent,
        NewRealTimeChartComponent,
        NewRealTimeContainerComponent,
        NewRealTimeTotalChartComponent,
        NewRealTimePagingContainerComponent
    ],
    imports: [
        SharedModule,
        HelpViewerPopupModule
    ],
    exports: [
        NewRealTimeContainerComponent,
        NewRealTimePagingContainerComponent
    ],
    providers: [
        NewRealTimeWebSocketService
    ]
})
export class NewRealTimeModule { }
