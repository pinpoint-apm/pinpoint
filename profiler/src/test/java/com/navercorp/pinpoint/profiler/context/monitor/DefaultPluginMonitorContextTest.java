/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.monitor;

import com.navercorp.pinpoint.bootstrap.plugin.monitor.DataSourceMonitor;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Taejin Koo
 */
public class DefaultPluginMonitorContextTest {

    @Test
    public void registerTest1() {

        DataSourceMonitorRegistryService dataSourceMonitorRegistryService = new DefaultDataSourceMonitorRegistryService(20);
        int remainingCapacity = dataSourceMonitorRegistryService.getRemainingIdNumber();

        MockDataSourceMonitor[] mockDataSourceMonitors = createMockDataSourceMonitor(dataSourceMonitorRegistryService, remainingCapacity);
        assertThat(dataSourceMonitorRegistryService.getPluginMonitorWrapperList()).hasSize(remainingCapacity);

        addOverDataSourceMonitor(dataSourceMonitorRegistryService);
        assertThat(dataSourceMonitorRegistryService.getPluginMonitorWrapperList()).hasSize(remainingCapacity);

        for (MockDataSourceMonitor mockMonitor : mockDataSourceMonitors) {
            boolean unregister = dataSourceMonitorRegistryService.unregister(mockMonitor);
            Assertions.assertTrue(unregister);
        }
        assertThat(dataSourceMonitorRegistryService.getPluginMonitorWrapperList()).isEmpty();

    }

    @Test
    public void registerTest2() {
        DataSourceMonitorRegistryService dataSourceMonitorRegistryService = new DefaultDataSourceMonitorRegistryService(20);
        int remainingCapacity = dataSourceMonitorRegistryService.getRemainingIdNumber();

        MockDataSourceMonitor[] mockDataSourceMonitors = createMockDataSourceMonitor(dataSourceMonitorRegistryService, remainingCapacity);
        assertThat(dataSourceMonitorRegistryService.getPluginMonitorWrapperList()).hasSize(remainingCapacity);

        addOverDataSourceMonitor(dataSourceMonitorRegistryService);
        assertThat(dataSourceMonitorRegistryService.getPluginMonitorWrapperList()).hasSize(remainingCapacity);

        for (MockDataSourceMonitor mockMonitor : mockDataSourceMonitors) {
            mockMonitor.close();
        }
        assertThat(dataSourceMonitorRegistryService.getPluginMonitorWrapperList()).isEmpty();
    }

    private MockDataSourceMonitor[] createMockDataSourceMonitor(DataSourceMonitorRegistryService dataSourceMonitorRegistry, int remainingCapacity) {
        MockDataSourceMonitor[] mockDataSourceMonitors = new MockDataSourceMonitor[remainingCapacity];
        for (int i = 0; i < remainingCapacity; i++) {
            MockDataSourceMonitor mock = new MockDataSourceMonitor();
            boolean register = dataSourceMonitorRegistry.register(mock);
            Assertions.assertTrue(register);
            mockDataSourceMonitors[i] = mock;
        }
        return mockDataSourceMonitors;
    }

    private void addOverDataSourceMonitor(DataSourceMonitorRegistryService dataSourceMonitorRegistry) {
        Random random = new Random(System.currentTimeMillis());
        int additionalRegisterCount = random.nextInt(10);
        for (int i = 0; i < additionalRegisterCount; i++) {
            MockDataSourceMonitor mock = new MockDataSourceMonitor();
            boolean register = dataSourceMonitorRegistry.register(mock);
            Assertions.assertFalse(register);
        }
    }

    private static class MockDataSourceMonitor implements DataSourceMonitor {

        private boolean closed = false;

        @Override
        public String getUrl() {
            return "url";
        }

        @Override
        public int getActiveConnectionSize() {
            return 0;
        }

        @Override
        public int getMaxConnectionSize() {
            return 10;
        }

        @Override
        public ServiceType getServiceType() {
            return ServiceType.UNKNOWN;
        }

        @Override
        public boolean isDisabled() {
            return closed;
        }

        public void close() {
            closed = true;
        }

    }

}
