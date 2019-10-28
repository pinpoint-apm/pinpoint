import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ScatterChartSettingPopupComponent } from './scatter-chart-setting-popup.component';
import { ScatterChartOptionsComponent } from './scatter-chart-options.component';
import { ScatterChartStateViewComponent } from './scatter-chart-state-view.component';
import { ScatterChartComponent } from './scatter-chart.component';
import { ScatterChartContainerComponent } from './scatter-chart-container.component';
import { ScatterChartForFilteredMapSideBarContainerComponent } from './scatter-chart-for-filtered-map-side-bar-container.component';
import { ScatterChartForFilteredMapInfoPerServerContainerComponent } from './scatter-chart-for-filtered-map-info-per-server-container.component';
import { ScatterChartForInfoPerServerContainerComponent } from './scatter-chart-for-info-per-server-container.component';
import { ScatterChartForFullScreenModeContainerComponent } from './scatter-chart-for-full-screen-mode-container.component';
import { ScatterChartInteractionService } from './scatter-chart-interaction.service';
import { ScatterChartDataService } from './scatter-chart-data.service';
import { HelpViewerPopupModule } from 'app/core/components/help-viewer-popup';

@NgModule({
    declarations: [
        ScatterChartSettingPopupComponent,
        ScatterChartOptionsComponent,
        ScatterChartStateViewComponent,
        ScatterChartComponent,
        ScatterChartContainerComponent,
        ScatterChartForFilteredMapSideBarContainerComponent,
        ScatterChartForFilteredMapInfoPerServerContainerComponent,
        ScatterChartForInfoPerServerContainerComponent,
        ScatterChartForFullScreenModeContainerComponent
    ],
    imports: [
        HelpViewerPopupModule,
        SharedModule
    ],
    exports: [
        ScatterChartContainerComponent,
        ScatterChartForFilteredMapSideBarContainerComponent,
        ScatterChartForFilteredMapInfoPerServerContainerComponent,
        ScatterChartForInfoPerServerContainerComponent,
        ScatterChartForFullScreenModeContainerComponent
    ],
    providers: [
        ScatterChartInteractionService,
        ScatterChartDataService
    ]
})
export class ScatterChartModule { }
