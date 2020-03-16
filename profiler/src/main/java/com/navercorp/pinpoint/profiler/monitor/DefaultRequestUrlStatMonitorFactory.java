/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor;

import com.navercorp.pinpoint.bootstrap.plugin.monitor.RequestUrlMappingExtractor;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.RequestUrlMappingExtractorFactory;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.RequestUrlMappingExtractorProviderLocator;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.RequestUrlStatMonitor;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.RequestUrlStatMonitorFactory;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.sender.DataSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class DefaultRequestUrlStatMonitorFactory implements RequestUrlStatMonitorFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DataSender dataSender;
    private RequestUrlMappingExtractorProviderLocator requestUrlMappingExtractorProviderLocator;

    private final Map<String, RequestUrlStatMonitor> createdRequestUrlStatMonitorMap = new HashMap<String, RequestUrlStatMonitor>();

    public DefaultRequestUrlStatMonitorFactory(DataSender dataSender) {
        this.dataSender = Assert.requireNonNull(dataSender, "dataSender");
    }

    @Override
    public void setRequestUrlMappingExtractorProviderLocator(RequestUrlMappingExtractorProviderLocator requestUrlMappingExtractorProviderLocator) {
        this.requestUrlMappingExtractorProviderLocator = requestUrlMappingExtractorProviderLocator;
    }

    @Override
    public <T> RequestUrlStatMonitor<T> create(RequestUrlMappingExtractorFactory<T> requestUrlMappingExtractorFactory) {
        Assert.requireNonNull(requestUrlMappingExtractorFactory, "requestUrlMappingExtractorFactory");
        if (requestUrlMappingExtractorProviderLocator == null) {
            return null;
        }

        synchronized (DefaultRequestUrlStatMonitorFactory.class) {
            String mappingExtractorFactoryName = requestUrlMappingExtractorFactory.getName();
            RequestUrlStatMonitor requestUrlStatMonitor = createdRequestUrlStatMonitorMap.get(mappingExtractorFactoryName);
            if (requestUrlStatMonitor != null) {
                return requestUrlStatMonitor;
            }

            RequestUrlMappingExtractor<T> requestUrlMappingExtractor = requestUrlMappingExtractorFactory.create(requestUrlMappingExtractorProviderLocator.getProviderList());
            requestUrlStatMonitor = new DefaultRequestUrlStatMonitor<T>(dataSender, requestUrlMappingExtractor);

            if (requestUrlStatMonitor != null) {
                createdRequestUrlStatMonitorMap.put(mappingExtractorFactoryName, requestUrlStatMonitor);
            }
            return requestUrlStatMonitor;
        }
    }

    @Override
    public RequestUrlMappingExtractorProviderLocator getRequestUrlMappingExtractorProviderLocator() {
        return requestUrlMappingExtractorProviderLocator;
    }

    @Override
    public void releaseResources() {
        logger.info("releaseResources started()");
        Collection<RequestUrlStatMonitor> requestUrlStatMonitorList = createdRequestUrlStatMonitorMap.values();
        for (RequestUrlStatMonitor requestUrlStatMonitor : requestUrlStatMonitorList) {
            requestUrlStatMonitor.close();
        }
        createdRequestUrlStatMonitorMap.clear();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultRequestUrlStatMonitorFactory{");
        sb.append("dataSender=").append(dataSender);
        sb.append(", requestUrlMappingExtractorProviderLocator=").append(requestUrlMappingExtractorProviderLocator);
        sb.append('}');
        return sb.toString();
    }

}

