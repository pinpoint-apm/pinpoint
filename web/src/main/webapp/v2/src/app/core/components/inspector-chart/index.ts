
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { RetryComponent } from './retry.component';
import { NoDataComponent } from './no-data.component';
import { InspectorChartComponent } from './inspector-chart.component';
import { AgentActiveThreadChartContainerComponent } from './agent-active-thread-chart-container.component';
import { AgentCPUChartContainerComponent } from './agent-cpu-chart-container.component';
import { AgentDataSourceChartContainerComponent } from './agent-data-source-chart-container.component';
import { AgentJVMHeapChartContainerComponent } from './agent-jvm-heap-chart-container.component';
import { AgentJVMNonHeapChartContainerComponent } from './agent-jvm-non-heap-chart-container.component';
import { AgentResponseTimeChartContainerComponent } from './agent-response-time-chart-container.component';
import { AgentTPSChartContainerComponent } from './agent-tps-chart-container.component';
import { AgentDataSourceChartInfotableComponent } from './agent-data-source-chart-infotable.component';
import { AgentDataSourceChartSelectSourceComponent } from './agent-data-source-chart-select-source.component';
import { AgentOpenFileDescriptorChartContainerComponent } from 'app/core/components/inspector-chart/agent-open-file-descriptor-chart-container.component';
import { AgentDirectBufferCountChartContainerComponent } from 'app/core/components/inspector-chart/agent-direct-buffer-count-chart-container.component';
import { AgentDirectBufferMemoryChartContainerComponent } from 'app/core/components/inspector-chart/agent-direct-buffer-memory-chart-container.component';
import { AgentMappedBufferCountChartContainerComponent } from 'app/core/components/inspector-chart/agent-mapped-buffer-count-chart-container.component';
import { AgentMappedBufferMemoryChartContainerComponent } from 'app/core/components/inspector-chart/agent-mapped-buffer-memory-chart-container.component';
import { ApplicationActiveThreadChartContainerComponent } from './application-active-thread-chart-container.component';
import { ApplicationJVMCPUChartContainerComponent } from './application-jvm-cpu-chart-container.component';
import { ApplicationJVMHeapChartContainerComponent } from './application-jvm-heap-chart-container.component';
import { ApplicationJVMNonHeapChartContainerComponent } from './application-jvm-non-heap-chart-container.component';
import { ApplicationResponseTimeChartContainerComponent } from './application-response-time-chart-container.component';
import { ApplicationSystemCPUChartContainerComponent } from './application-system-cpu-chart-container.component';
import { ApplicationDataSourceChartContainerComponent } from 'app/core/components/inspector-chart/application-data-source-chart-container.component';
import { ApplicationTPSChartContainerComponent } from './application-tps-chart-container.component';
import { ApplicationOpenFileDescriptorChartContainerComponent } from 'app/core/components/inspector-chart/application-open-file-descriptor-chart-container.component';
import { ApplicationDataSourceChartSourcelistComponent } from './application-data-source-chart-soucelist.component';
import { ApplicationDirectBufferCountChartContainerComponent } from 'app/core/components/inspector-chart/application-direct-buffer-count-chart-container.component';
import { ApplicationDirectBufferMemoryChartContainerComponent } from 'app/core/components/inspector-chart/application-direct-buffer-memory-chart-container.component';
import { ApplicationMappedBufferCountChartContainerComponent } from 'app/core/components/inspector-chart/application-mapped-buffer-count-chart-container.component';
import { ApplicationMappedBufferMemoryChartContainerComponent } from 'app/core/components/inspector-chart/application-mapped-buffer-memory-chart-container.component';
import { TransactionViewJVMHeapChartContainerComponent } from './transaction-view-jvm-heap-chart-container.component';
import { TransactionViewJVMNonHeapChartContainerComponent } from './transaction-view-jvm-non-heap-chart-container.component';
import { TransactionViewCPUChartContainerComponent } from './transaction-view-cpu-chart-container.component';

import { AgentActiveThreadChartDataService } from './agent-active-thread-chart-data.service';
import { AgentCPUChartDataService } from './agent-cpu-chart-data.service';
import { AgentDataSourceChartDataService } from './agent-data-source-chart-data.service';
import { AgentMemoryChartDataService } from './agent-memory-chart-data.service';
import { AgentTPSChartDataService } from './agent-tps-chart-data.service';
import { ApplicationCPUChartDataService } from './application-cpu-chart-data.service';
import { ApplicationActiveThreadChartDataService } from './application-active-thread-chart-data.service';
import { ApplicationDataSourceChartDataService } from './application-data-source-chart-data.service';
import { ApplicationMemoryChartDataService } from './application-memory-chart-data.service';
import { ApplicationTPSChartDataService } from './application-tps-chart-data.service';
import { ApplicationResponseTimeChartDataService } from './application-response-time-chart-data.service';
import { AgentResponseTimeChartDataService } from './agent-response-time-chart-data.service';
import { TransactionViewMemoryChartDataService } from './transaction-view-memory-chart-data.service';
import { TransactionViewCPUChartDataService } from './transaction-view-cpu-chart-data.service';
import { AgentOpenFileDescriptorChartDataService } from 'app/core/components/inspector-chart/agent-open-file-descriptor-chart-data.service';
import { ApplicationOpenFileDescriptorChartDataService } from 'app/core/components/inspector-chart/application-open-file-descriptor-chart-data.service';
import { AgentDirectBufferChartDataService } from 'app/core/components/inspector-chart/agent-direct-buffer-chart-data.service';
import { ApplicationDirectBufferChartDataService } from 'app/core/components/inspector-chart/application-direct-buffer-chart-data.service';
import { HelpViewerPopupModule } from 'app/core/components/help-viewer-popup';

@NgModule({
    declarations: [
        RetryComponent,
        NoDataComponent,
        InspectorChartComponent,
        AgentActiveThreadChartContainerComponent,
        AgentCPUChartContainerComponent,
        AgentDataSourceChartContainerComponent,
        AgentJVMHeapChartContainerComponent,
        AgentJVMNonHeapChartContainerComponent,
        AgentResponseTimeChartContainerComponent,
        AgentTPSChartContainerComponent,
        ApplicationActiveThreadChartContainerComponent,
        ApplicationJVMCPUChartContainerComponent,
        ApplicationJVMHeapChartContainerComponent,
        ApplicationJVMNonHeapChartContainerComponent,
        ApplicationResponseTimeChartContainerComponent,
        ApplicationSystemCPUChartContainerComponent,
        ApplicationTPSChartContainerComponent,
        ApplicationDataSourceChartContainerComponent,
        AgentDataSourceChartInfotableComponent,
        AgentDataSourceChartSelectSourceComponent,
        ApplicationDataSourceChartSourcelistComponent,
        TransactionViewJVMHeapChartContainerComponent,
        TransactionViewJVMNonHeapChartContainerComponent,
        TransactionViewCPUChartContainerComponent,
        AgentOpenFileDescriptorChartContainerComponent,
        ApplicationOpenFileDescriptorChartContainerComponent,
        AgentDirectBufferCountChartContainerComponent,
        AgentDirectBufferMemoryChartContainerComponent,
        AgentMappedBufferCountChartContainerComponent,
        AgentMappedBufferMemoryChartContainerComponent,
        ApplicationDirectBufferCountChartContainerComponent,
        ApplicationDirectBufferMemoryChartContainerComponent,
        ApplicationMappedBufferCountChartContainerComponent,
        ApplicationMappedBufferMemoryChartContainerComponent
    ],
    imports: [
        SharedModule,
        HelpViewerPopupModule
    ],
    exports: [
        AgentActiveThreadChartContainerComponent,
        AgentCPUChartContainerComponent,
        AgentDataSourceChartContainerComponent,
        AgentJVMHeapChartContainerComponent,
        AgentJVMNonHeapChartContainerComponent,
        AgentResponseTimeChartContainerComponent,
        AgentTPSChartContainerComponent,
        ApplicationActiveThreadChartContainerComponent,
        ApplicationJVMCPUChartContainerComponent,
        ApplicationJVMHeapChartContainerComponent,
        ApplicationJVMNonHeapChartContainerComponent,
        ApplicationResponseTimeChartContainerComponent,
        ApplicationSystemCPUChartContainerComponent,
        ApplicationTPSChartContainerComponent,
        ApplicationDataSourceChartContainerComponent,
        AgentOpenFileDescriptorChartContainerComponent,
        ApplicationOpenFileDescriptorChartContainerComponent,
        AgentDirectBufferCountChartContainerComponent,
        AgentDirectBufferMemoryChartContainerComponent,
        AgentMappedBufferCountChartContainerComponent,
        AgentMappedBufferMemoryChartContainerComponent,
        ApplicationDirectBufferCountChartContainerComponent,
        ApplicationDirectBufferMemoryChartContainerComponent,
        ApplicationMappedBufferCountChartContainerComponent,
        ApplicationMappedBufferMemoryChartContainerComponent
    ],
    entryComponents: [
        TransactionViewJVMHeapChartContainerComponent,
        TransactionViewJVMNonHeapChartContainerComponent,
        TransactionViewCPUChartContainerComponent
    ],
    providers: [
        AgentActiveThreadChartDataService,
        AgentCPUChartDataService,
        AgentDataSourceChartDataService,
        AgentMemoryChartDataService,
        AgentTPSChartDataService,
        AgentResponseTimeChartDataService,
        AgentOpenFileDescriptorChartDataService,
        AgentDirectBufferChartDataService,
        ApplicationCPUChartDataService,
        ApplicationActiveThreadChartDataService,
        ApplicationDataSourceChartDataService,
        ApplicationMemoryChartDataService,
        ApplicationTPSChartDataService,
        ApplicationResponseTimeChartDataService,
        ApplicationOpenFileDescriptorChartDataService,
        ApplicationDirectBufferChartDataService,
        TransactionViewMemoryChartDataService,
        TransactionViewCPUChartDataService
    ]
})
export class InspectorChartModule { }
