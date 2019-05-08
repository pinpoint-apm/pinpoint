
import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { LoadChartComponent } from './load-chart.component';
import { LoadChartContainerComponent } from './load-chart-container.component';
import { HelpViewerPopupModule } from 'app/core/components/help-viewer-popup';
import { LoadChartChangeNotificationService } from './load-chart-change-notification.service';

@NgModule({
    declarations: [
        LoadChartComponent,
        LoadChartContainerComponent
    ],
    imports: [
        SharedModule,
        HelpViewerPopupModule
    ],
    exports: [
        LoadChartContainerComponent
    ],
    providers: [
        LoadChartChangeNotificationService
    ]
})
export class LoadChartModule { }
