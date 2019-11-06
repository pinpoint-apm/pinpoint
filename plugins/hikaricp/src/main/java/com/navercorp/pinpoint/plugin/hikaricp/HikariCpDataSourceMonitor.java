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

package com.navercorp.pinpoint.plugin.hikaricp;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.DataSourceMonitor;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.lang.reflect.Method;

/**
 * @author Taejin Koo
 */
public class HikariCpDataSourceMonitor implements DataSourceMonitor {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private final String jdbcUrl;
    private final Object object;

    private final Method getActiveConnectionsMethod;
    private final Method getTotalConnectionsMethod;


    public HikariCpDataSourceMonitor(Object object, String jdbcUrl) {
        this.object = object;
        this.jdbcUrl = jdbcUrl;

        try {
            this.getActiveConnectionsMethod = getActiveConnectionsMethod(object);
            this.getTotalConnectionsMethod = getTotalConnectionsMethod(object);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private volatile boolean closed = false;

    private Method getActiveConnectionsMethod(Object object) throws NoSuchMethodException {
        Method getActiveConnectionsMethod = object.getClass().getMethod("getActiveConnections");
        if (getActiveConnectionsMethod == null) {
            throw new IllegalArgumentException("object must has getActiveConnections method");
        }


        Class<?> returnType = getActiveConnectionsMethod.getReturnType();
        if (int.class != returnType) {
            throw new IllegalArgumentException("invalid return type. expected:int, actual:" + returnType);
        }

        return getActiveConnectionsMethod;
    }

    private Method getTotalConnectionsMethod(Object object) throws NoSuchMethodException {
        Method getTotalConnections = object.getClass().getMethod("getTotalConnections");
        if (getTotalConnections == null) {
            throw new IllegalArgumentException("object must has getTotalConnections method");
        }

        Class<?> returnType = getTotalConnections.getReturnType();
        if (int.class != returnType) {
            throw new IllegalArgumentException("invalid return type. expected:int, actual:" + returnType);
        }

        return getTotalConnections;
    }

    @Override
    public ServiceType getServiceType() {
        return HikariCpConstants.SERVICE_TYPE;
    }

    @Override
    public String getUrl() {
        return jdbcUrl;
    }

    @Override
    public int getActiveConnectionSize() {
        try {
            Object result = getActiveConnectionsMethod.invoke(object);
            return (Integer) result;
        } catch (Exception e) {
            logger.info("failed while executing getActiveConnectionSize()");
        }
        return -1;
    }

    @Override
    public int getMaxConnectionSize() {
        try {
            Object result = getTotalConnectionsMethod.invoke(object);
            return (Integer) result;
        } catch (Exception e) {
            logger.info("failed while executing getActiveConnectionSize()");
        }
        return -1;
    }

    @Override
    public boolean isDisabled() {
        return closed;
    }

    public void close() {
        closed = true;
    }

}
