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

package com.navercorp.pinpoint.bootstrap.plugin.monitor;

import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author Taejin Koo
 */
public class DataSourcePluginMonitorProxy implements DataSourcePluginMonitor, PluginMonitorProxy<DataSourcePluginMonitor> {

    private final int id;
    private final DataSourcePluginMonitor dataSourceMonitor;

    public DataSourcePluginMonitorProxy(int id, DataSourcePluginMonitor dataSourceMonitor) {
        if (dataSourceMonitor == null) {
            throw new NullPointerException("dataSourceMonitor may not be null");
        }

        this.id = id;
        this.dataSourceMonitor = dataSourceMonitor;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean isDisabled() {
        return dataSourceMonitor.isDisabled();
    }

    @Override
    public ServiceType getServiceType() {
        return dataSourceMonitor.getServiceType();
    }

    @Override
    public String getName() {
        return dataSourceMonitor.getName();
    }

    @Override
    public String getUrl() {
        return dataSourceMonitor.getUrl();
    }

    @Override
    public int getActiveConnectionCount() {
        return dataSourceMonitor.getActiveConnectionCount();
    }

    @Override
    public int getMaxCount() {
        return dataSourceMonitor.getMaxCount();
    }

    @Override
    public DataSourcePluginMonitor getDelegate() {
        return dataSourceMonitor;
    }

}
