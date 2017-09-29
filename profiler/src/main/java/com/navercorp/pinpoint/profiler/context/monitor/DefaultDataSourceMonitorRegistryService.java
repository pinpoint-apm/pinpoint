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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Taejin Koo
 */
public class DefaultDataSourceMonitorRegistryService implements DataSourceMonitorRegistryService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final int limitIdNumber;

    private final CopyOnWriteArrayList<DataSourceMonitorWrapper> repository = new CopyOnWriteArrayList<DataSourceMonitorWrapper>();

    private final DataSourceMonitorWrapperFactory wrapperFactory = new DataSourceMonitorWrapperFactory();

    public DefaultDataSourceMonitorRegistryService(int limitIdNumber) {
        this.limitIdNumber = limitIdNumber;
    }

    @Override
    public boolean register(DataSourceMonitor dataSourceMonitor) {
        if (wrapperFactory.latestIssuedId() >= limitIdNumber) {
            if (logger.isInfoEnabled()) {
                logger.info("can't register {}. The maximum value of id number has been exceeded.");
            }
            return false;
        }

        DataSourceMonitorWrapper dataSourceMonitorWrapper = wrapperFactory.create(dataSourceMonitor);
        return repository.add(dataSourceMonitorWrapper);
    }

    @Override
    public boolean unregister(DataSourceMonitor dataSourceMonitor) {
        for (DataSourceMonitorWrapper dataSourceMonitorWrapper : repository) {
            if (dataSourceMonitorWrapper.equalsWithUnwrap(dataSourceMonitor)) {
                return repository.remove(dataSourceMonitorWrapper);
            }
        }
        return false;
    }

    @Override
    public List<DataSourceMonitorWrapper> getPluginMonitorWrapperList() {
        List<DataSourceMonitorWrapper> pluginMonitorList = new ArrayList<DataSourceMonitorWrapper>(repository.size());
        List<DataSourceMonitorWrapper> disabledPluginMonitorList = new ArrayList<DataSourceMonitorWrapper>();

        for (DataSourceMonitorWrapper dataSourceMonitorWrapper : repository) {
            if (dataSourceMonitorWrapper.isDisabled()) {
                disabledPluginMonitorList.add(dataSourceMonitorWrapper);
            } else {
                pluginMonitorList.add(dataSourceMonitorWrapper);
            }
        }

        // bulk delete for reduce copy
        if (disabledPluginMonitorList.size() > 0) {
            logger.info("PluginMonitorWrapper was disabled(list:{})", disabledPluginMonitorList);
            repository.removeAll(disabledPluginMonitorList);
        }

        return pluginMonitorList;
    }

    @Override
    public int getRemainingIdNumber() {
        return limitIdNumber - wrapperFactory.latestIssuedId();
    }

}
