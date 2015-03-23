/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.plugin.ServerTypeDetector;
import com.navercorp.pinpoint.bootstrap.resolver.ApplicationServerTypePluginResolver;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.profiler.plugin.DefaultProfilerPluginContext;

/**
 * @author emeroad
 * @author netspider
 * @author hyungil.jeong
 */
public class ApplicationServerTypeResolver {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ServiceType defaultType;
    private final ApplicationServerTypePluginResolver resolver;
    private final List<ServerTypeDetector> detectors = new ArrayList<ServerTypeDetector>();

    public ApplicationServerTypeResolver(List<DefaultProfilerPluginContext> plugins, ServiceType defaultType, List<String> orderedDetectors) {
        if (isValidApplicationServerType(defaultType)) {
            this.defaultType = defaultType;
        } else {
            this.defaultType = ServiceType.UNDEFINED;
        }
        Map<String, ServerTypeDetector> registeredDetectors = getRegisteredServerTypeDetectors(plugins);
        for (String orderedDetector : orderedDetectors) {
            if (registeredDetectors.containsKey(orderedDetector)) {
                this.detectors.add(registeredDetectors.remove(orderedDetector));
            }
        }
        this.detectors.addAll(registeredDetectors.values());
        this.resolver = new ApplicationServerTypePluginResolver(this.detectors);
    }
    
    private Map<String, ServerTypeDetector> getRegisteredServerTypeDetectors(List<DefaultProfilerPluginContext> plugins) {
        Map<String, ServerTypeDetector> registeredDetectors = new HashMap<String, ServerTypeDetector>();
        for (DefaultProfilerPluginContext context : plugins) {
            for (ServerTypeDetector detector : context.getServerTypeDetectors()) {
                registeredDetectors.put(detector.getClass().getName(), detector);
            }
        }
        return registeredDetectors;
    }
    
    public ServiceType resolve() {
        ServiceType resolvedApplicationServerType;
        if (this.defaultType == ServiceType.UNDEFINED) {
            resolvedApplicationServerType = this.resolver.resolve();
            logger.info("Resolved ApplicationServerType : {}", resolvedApplicationServerType.getName());
        } else {
            resolvedApplicationServerType = this.defaultType;
            logger.info("Configured ApplicationServerType : {}", resolvedApplicationServerType.getName());
        }
        return resolvedApplicationServerType;
    }
    
    private boolean isValidApplicationServerType(ServiceType serviceType) {
        if (serviceType == null) {
            return false;
        }
        return serviceType.isWas();
    }
}
