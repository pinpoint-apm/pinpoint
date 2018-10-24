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

import { NewRealTimeAgentChartComponent } from 'app/core/components/real-time/new/new-real-time-agent-chart.component';
import { NewRealTimeChartComponent } from 'app/core/components/real-time/new/new-real-time-chart.component';
import { NewRealTimeContainerComponent } from 'app/core/components/real-time/new/new-real-time-container.component';
import { NewRealTimeTotalChartComponent } from 'app/core/components/real-time/new/new-real-time-total-chart.component';

@NgModule({
    declarations: [
        ResizeTopDirective,
        RealTimeChartComponent,
        RealTimeAgentChartComponent,
        RealTimeTotalChartComponent,
        RealTimeContainerComponent,
        RealTimePagingContainerComponent,
        NewRealTimeAgentChartComponent,
        NewRealTimeChartComponent,
        NewRealTimeContainerComponent,
        NewRealTimeTotalChartComponent
    ],
    imports: [
        SharedModule,
        HelpViewerPopupModule
    ],
    exports: [
        RealTimeContainerComponent,
        NewRealTimeContainerComponent,
        RealTimePagingContainerComponent
    ],
    entryComponents: [
        RealTimeTotalChartComponent,
        RealTimeAgentChartComponent,
        NewRealTimeAgentChartComponent,
        NewRealTimeTotalChartComponent
    ],
    providers: [
        RealTimeWebSocketService
    ]
})
export class RealTimeModule { }
