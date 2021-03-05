import { PrimitiveArray, Data } from 'billboard.js';
import { Observable } from 'rxjs';

import { InspectorChartDataService } from './inspector-chart-data.service';
import { AgentJVMHeapChartContainer } from './agent-jvm-heap-chart-container';
import { AgentJVMNonHeapChartContainer } from './agent-jvm-non-heap-chart-container';
import { AgentCPUChartContainer } from './agent-cpu-chart-container';
import { AgentTPSChartContainer } from './agent-tps-chart-container';
import { AgentActiveRequestChartContainer } from './agent-active-request-chart-container';
import { AgentTotalThreadCountChartContainer} from './agent-total-thread-count-chart-container';
import { AgentResponseTimeChartContainer } from './agent-response-time-chart-container';
import { AgentOpenFileDescriptorChartContainer } from './agent-open-file-descriptor-chart-container';
import { AgentDirectBufferCountChartContainer } from './agent-direct-buffer-count-chart-container';
import { AgentDirectBufferMemoryChartContainer } from './agent-direct-buffer-memory-chart-container';
import { AgentMappedBufferCountChartContainer } from './agent-mapped-buffer-count-chart-container';
import { AgentMappedBufferMemoryChartContainer } from './agent-mapped-buffer-memory-chart-container';
import { AgentLoadedCLassCountChartContainer } from './agent-loaded-class-count-chart-container';
import { AgentUnloadedCLassCountChartContainer } from './agent-unloaded-class-count-chart-container';
import { ApplicationJVMHeapChartContainer } from './application-jvm-heap-chart-container';
import { ApplicationJVMNonHeapChartContainer } from './application-jvm-non-heap-chart-container';
import { ApplicationJVMCpuChartContainer } from './application-jvm-cpu-chart-container';
import { ApplicationSystemCpuChartContainer } from './application-system-cpu-chart-container';
import { ApplicationTPSChartContainer } from './application-tps-chart-container';
import { ApplicationActiveRequestChartContainer } from './application-active-request-chart-container';
import { ApplicationTotalThreadCountChartContainer } from './application-total-thread-count-chart-container';
import { ApplicationResponseTimeChartContainer } from './application-response-time-chart-container';
import { ApplicationOpenFileDescriptorChartContainer } from './application-open-file-descriptor-chart-container';
import { ApplicationDirectBufferCountChartContainer } from './application-direct-buffer-count-chart-container';
import { ApplicationDirectBufferMemoryChartContainer } from './application-direct-buffer-memory-chart-container';
import { ApplicationMappedBufferCountChartContainer } from './application-mapped-buffer-count-chart-container';
import { ApplicationMappedBufferMemoryChartContainer } from './application-mapped-buffer-memory-chart-container';
import { ApplicationLoadedClassCountChartContainer } from './application-loaded-class-count-chart-container';
import { ApplicationUnloadedClassCountChartContainer } from './application-unloaded-class-count-chart-container';
import { IInspectorChartData } from './inspector-chart-data.service';

export interface IInspectorChartContainer {
    title: string;
    defaultYMax: number;

    getData(range: number[]): Observable<IInspectorChartData | AjaxException>;
    makeChartData(data: IInspectorChartData): PrimitiveArray[];
    makeDataOption(): Data;
    makeElseOption(): {[key: string]: any};
    makeYAxisOptions(data: PrimitiveArray[]): {[key: string]: any};
    convertWithUnit(value: number): string;
    getTooltipFormat(value: number, columnId: string, i: number): string;
}

export enum ChartType {
    AGENT_JVM_HEAP = 'AGENT_JVM_HEAP',
    AGENT_JVM_NON_HEAP = 'AGENT_JVM_NON_HEAP',
    AGENT_CPU = 'AGENT_CPU',
    AGENT_TPS = 'AGENT_TPS',
    AGENT_ACTIVE_REQUEST = 'AGENT_ACTIVE_REQUEST',
    AGENT_TOTAL_THREAD = 'AGENT_TOTAL_THREAD',
    AGENT_RESPONSE_TIME = 'AGENT_RESPONSE_TIME',
    AGENT_OPEN_FILE_DESCRIPTOR = 'AGENT_OPEN_FILE_DESCRIPTOR',
    AGENT_DIRECT_BUFFER_COUNT = 'AGENT_DIRECT_BUFFER_COUNT',
    AGENT_DIRECT_BUFFER_MEMORY = 'AGENT_DIRECT_BUFFER_MEMORY',
    AGENT_MAPPED_BUFFER_COUNT = 'AGENT_MAPPED_BUFFER_COUNT',
    AGENT_MAPPED_BUFFER_MEMORY = 'AGENT_MAPPED_BUFFER_MEMORY',
    AGENT_DATA_SOURCE = 'AGENT_DATA_SOURCE',
    AGENT_LOADED_CLASS_COUNT = 'AGENT_LOADED_CLASS_COUNT',
    AGENT_UNLOADED_CLASS_COUNT = 'AGENT_UNLOADED_CLASS_COUNT',
    APPLICATION_JVM_HEAP = 'APPLICATION_JVM_HEAP',
    APPLICATION_JVM_NON_HEAP = 'APPLICATION_JVM_NON_HEAP',
    APPLICATION_JVM_CPU = 'APPLICATION_JVM_CPU',
    APPLICATION_SYSTEM_CPU = 'APPLICATION_SYSTEM_CPU',
    APPLICATION_TPS = 'APPLICATION_TPS',
    APPLICATION_ACTIVE_REQUEST = 'APPLICATION_ACTIVE_REQUEST',
    APPLICATION_TOTAL_THREAD = 'APPLICATION_TOTAL_THREAD',
    APPLICATION_RESPONSE_TIME = 'APPLICATION_RESPONSE_TIME',
    APPLICATION_OPEN_FILE_DESCRIPTOR = 'APPLICATION_OPEN_FILE_DESCRIPTOR',
    APPLICATION_DIRECT_BUFFER_COUNT = 'APPLICATION_DIRECT_BUFFER_COUNT',
    APPLICATION_DIRECT_BUFFER_MEMORY = 'APPLICATION_DIRECT_BUFFER_MEMORY',
    APPLICATION_MAPPED_BUFFER_COUNT = 'APPLICATION_MAPPED_BUFFER_COUNT',
    APPLICATION_MAPPED_BUFFER_MEMORY = 'APPLICATION_MAPPED_BUFFER_MEMORY',
    APPLICATION_DATA_SOURCE = 'APPLICATION_DATA_SOURCE',
    APPLICATION_LOADED_CLASS_COUNT = 'APPLICATION_LOADED_CLASS_COUNT',
    APPLICATION_UNLOADED_CLASS_COUNT = 'APPLICATION_UNLOADED_CLASS_COUNT'
}

export class InspectorChartContainerFactory {
    static createInspectorChartContainer(chartType: ChartType, dataService: InspectorChartDataService): IInspectorChartContainer {
        switch (chartType) {
            case ChartType.AGENT_JVM_HEAP:
                return new AgentJVMHeapChartContainer(dataService);
            case ChartType.AGENT_JVM_NON_HEAP:
                return new AgentJVMNonHeapChartContainer(dataService);
            case ChartType.AGENT_CPU:
                return new AgentCPUChartContainer(dataService);
            case ChartType.AGENT_TPS:
                return new AgentTPSChartContainer(dataService);
            case ChartType.AGENT_ACTIVE_REQUEST:
                return new AgentActiveRequestChartContainer(dataService);
            case ChartType.AGENT_TOTAL_THREAD:
                return new AgentTotalThreadCountChartContainer(dataService);
            case ChartType.AGENT_RESPONSE_TIME:
                return new AgentResponseTimeChartContainer(dataService);
            case ChartType.AGENT_OPEN_FILE_DESCRIPTOR:
                return new AgentOpenFileDescriptorChartContainer(dataService);
            case ChartType.AGENT_DIRECT_BUFFER_COUNT:
                return new AgentDirectBufferCountChartContainer(dataService);
            case ChartType.AGENT_DIRECT_BUFFER_MEMORY:
                return new AgentDirectBufferMemoryChartContainer(dataService);
            case ChartType.AGENT_MAPPED_BUFFER_COUNT:
                return new AgentMappedBufferCountChartContainer(dataService);
            case ChartType.AGENT_MAPPED_BUFFER_MEMORY:
                return new AgentMappedBufferMemoryChartContainer(dataService);
            case ChartType.AGENT_LOADED_CLASS_COUNT:
                return new AgentLoadedCLassCountChartContainer(dataService);
            case ChartType.AGENT_UNLOADED_CLASS_COUNT:
                return new AgentUnloadedCLassCountChartContainer(dataService);
            case ChartType.APPLICATION_JVM_HEAP:
                return new ApplicationJVMHeapChartContainer(dataService);
            case ChartType.APPLICATION_JVM_NON_HEAP:
                return new ApplicationJVMNonHeapChartContainer(dataService);
            case ChartType.APPLICATION_JVM_CPU:
                return new ApplicationJVMCpuChartContainer(dataService);
            case ChartType.APPLICATION_SYSTEM_CPU:
                return new ApplicationSystemCpuChartContainer(dataService);
            case ChartType.APPLICATION_TPS:
                return new ApplicationTPSChartContainer(dataService);
            case ChartType.APPLICATION_ACTIVE_REQUEST:
                return new ApplicationActiveRequestChartContainer(dataService);
            case ChartType.APPLICATION_TOTAL_THREAD:
                return new ApplicationTotalThreadCountChartContainer(dataService);
            case ChartType.APPLICATION_RESPONSE_TIME:
                return new ApplicationResponseTimeChartContainer(dataService);
            case ChartType.APPLICATION_OPEN_FILE_DESCRIPTOR:
                return new ApplicationOpenFileDescriptorChartContainer(dataService);
            case ChartType.APPLICATION_DIRECT_BUFFER_COUNT:
                return new ApplicationDirectBufferCountChartContainer(dataService);
            case ChartType.APPLICATION_DIRECT_BUFFER_MEMORY:
                return new ApplicationDirectBufferMemoryChartContainer(dataService);
            case ChartType.APPLICATION_MAPPED_BUFFER_COUNT:
                return new ApplicationMappedBufferCountChartContainer(dataService);
            case ChartType.APPLICATION_MAPPED_BUFFER_MEMORY:
                return new ApplicationMappedBufferMemoryChartContainer(dataService);
            case ChartType.APPLICATION_LOADED_CLASS_COUNT:
                return new ApplicationLoadedClassCountChartContainer(dataService);
            case ChartType.APPLICATION_UNLOADED_CLASS_COUNT:
                return new ApplicationUnloadedClassCountChartContainer(dataService);
        }
    }
}
