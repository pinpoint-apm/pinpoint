
import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { InspectorChartComponent } from './inspector-chart.component';
import { AgentDataSourceChartContainerComponent } from './agent-data-source-chart-container.component';
import { AgentDataSourceChartInfotableComponent } from './agent-data-source-chart-infotable.component';
import { AgentDataSourceChartSelectSourceComponent } from './agent-data-source-chart-select-source.component';
import { ApplicationDataSourceChartContainerComponent } from 'app/core/components/inspector-chart/application-data-source-chart-container.component';
import { ApplicationDataSourceChartSourcelistComponent } from './application-data-source-chart-soucelist.component';
import { AgentDataSourceChartDataService } from './agent-data-source-chart-data.service';
import { ApplicationDataSourceChartDataService } from './application-data-source-chart-data.service';
import { HelpViewerPopupModule } from 'app/core/components/help-viewer-popup';
import { InspectorChartContainerComponent } from './inspector-chart-container.component';
import { InspectorChartDataService } from './inspector-chart-data.service';
import { TransactionViewChartContainerComponent } from './transaction-view-chart-container.component';

@NgModule({
    declarations: [
        InspectorChartComponent,
        AgentDataSourceChartContainerComponent,
        ApplicationDataSourceChartContainerComponent,
        AgentDataSourceChartInfotableComponent,
        AgentDataSourceChartSelectSourceComponent,
        ApplicationDataSourceChartSourcelistComponent,
        InspectorChartContainerComponent,
        TransactionViewChartContainerComponent
    ],
    imports: [
        SharedModule,
        HelpViewerPopupModule
    ],
    exports: [
        AgentDataSourceChartContainerComponent,
        ApplicationDataSourceChartContainerComponent,
        InspectorChartContainerComponent
    ],
    entryComponents: [
        TransactionViewChartContainerComponent
    ],
    providers: [
        AgentDataSourceChartDataService,
        ApplicationDataSourceChartDataService,
        InspectorChartDataService
    ]
})
export class InspectorChartModule { }
