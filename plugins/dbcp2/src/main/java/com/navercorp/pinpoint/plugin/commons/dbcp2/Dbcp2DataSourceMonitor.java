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

package com.navercorp.pinpoint.plugin.commons.dbcp2;

import com.navercorp.pinpoint.bootstrap.plugin.monitor.DataSourceMonitor;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 * @author Taejin Koo
 */
public class Dbcp2DataSourceMonitor implements DataSourceMonitor {

    private final BasicDataSource dataSource;
    private volatile boolean closed = false;

    public Dbcp2DataSourceMonitor(BasicDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public ServiceType getServiceType() {
        return CommonsDbcp2Constants.SERVICE_TYPE;
    }

    @Override
    public String getUrl() {
        return dataSource.getUrl();
    }

    @Override
    public int getActiveConnectionSize() {
        return dataSource.getNumActive();
    }

    @Override
    public int getMaxConnectionSize() {
        return dataSource.getMaxTotal();
    }

    @Override
    public boolean isDisabled() {
        return closed;
    }

    public void close() {
        closed = true;
    }

}
