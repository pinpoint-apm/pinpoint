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

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.DataSourceMonitor;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.lang.reflect.Method;

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

    private final Object dataSource;

    private final Method getActiveCountMethod;

    private final Method getMaxActiveMethod;

    private final Method getUrlMethod;

    /**
     * Instantiates a new Druid data source monitor.
     *
     * @param dataSource the data source
     */
    public DruidDataSourceMonitor(Object dataSource) {

        this.dataSource = dataSource;
        try {
            this.getUrlMethod = getUrlMethod(dataSource);
            this.getMaxActiveMethod = getMaxActiveMethod(dataSource);
            this.getActiveCountMethod = getActiveCountMethod(dataSource);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private Method getUrlMethod(Object object) throws NoSuchMethodException {

        Method getUrlMethod = object.getClass().getMethod("getUrl");

        if (getUrlMethod == null) {

            throw new IllegalArgumentException("object must has getUrl method");
        }

        Class<?> returnType = getUrlMethod.getReturnType();

        if (String.class != returnType) {

            throw new IllegalArgumentException("invalid return type. expected:String, actual:" + returnType);
        }

        return getUrlMethod;
    }

    private Method getMaxActiveMethod(Object object) throws NoSuchMethodException {

        Method getMaxActiveMethod = object.getClass().getMethod("getMaxActive");

        if (getMaxActiveMethod == null) {

            throw new IllegalArgumentException("object must has getMaxActive method");
        }

        Class<?> returnType = getMaxActiveMethod.getReturnType();

        if (int.class != returnType) {

            throw new IllegalArgumentException("invalid return type. expected:int, actual:" + returnType);
        }

        return getMaxActiveMethod;
    }

    private Method getActiveCountMethod(Object object) throws NoSuchMethodException {

        Method getActiveCountMethod = object.getClass().getMethod("getActiveCount");

        if (getActiveCountMethod == null) {

            throw new IllegalArgumentException("object must has getActiveCount method");
        }

        Class<?> returnType = getActiveCountMethod.getReturnType();

        if (int.class != returnType) {

            throw new IllegalArgumentException("invalid return type. expected:int, actual:" + returnType);
        }

        return getActiveCountMethod;
    }

    @Override
    public ServiceType getServiceType() {
        return DruidConstants.SERVICE_TYPE;
    }

    @Override
    public String getUrl() {
        try {
            Object result = getUrlMethod.invoke(dataSource);
            return (String) result;
        } catch (Exception e) {
            logger.info("failed while executing getUrl()");
        }
        return null;
    }

    @Override
    public int getActiveConnectionSize() {
        try {
            Object result = getActiveCountMethod.invoke(dataSource);
            return (Integer) result;
        } catch (Exception e) {
            logger.info("failed while executing getActiveCount()");
        }
        return -1;
    }

    @Override
    public int getMaxConnectionSize() {
        try {
            Object result = getMaxActiveMethod.invoke(dataSource);
            return (Integer) result;
        } catch (Exception e) {
            logger.info("failed while executing getMaxActive()");
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