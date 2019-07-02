/*
 *  Copyright 2018 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.navercorp.pinpoint.plugin.druid;

import com.alibaba.druid.pool.DruidDataSource;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.DataSourceMonitor;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * The type Druid data source monitor.
 *
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2017/07/21
 */
public class DruidDataSourceMonitor implements DataSourceMonitor {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private volatile boolean closed = false;

    private final DruidDataSource dataSource;

    /**
     * Instantiates a new Druid data source monitor.
     *
     * @param dataSource the data source
     */
    public DruidDataSourceMonitor(Object dataSource) {

        if (dataSource instanceof DruidDataSource) {
            this.dataSource = (DruidDataSource) dataSource;
        } else {
            this.dataSource = null;
            logger.error("DataSource must be instance of DruidDataSource!");
        }
    }

    @Override
    public ServiceType getServiceType() {
        return DruidConstants.SERVICE_TYPE;
    }

    @Override
    public String getUrl() {
        if (dataSource != null) {
            return dataSource.getUrl();
        }
        return null;
    }

    @Override
    public int getActiveConnectionSize() {
        if (dataSource != null) {
            return dataSource.getActiveCount();
        }
        return -1;
    }

    @Override
    public int getMaxConnectionSize() {
        if (dataSource != null) {
            return dataSource.getMaxActive();
        }
        return -1;
    }

    @Override
    public boolean isDisabled() {
        return closed;
    }

    /**
     * Close.
     */
    public void close() {
        closed = true;
    }
}