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

import com.navercorp.pinpoint.bootstrap.plugin.ApplicationTypeDetector;
import com.navercorp.pinpoint.bootstrap.resolver.ApplicationServerTypePluginResolver;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author emeroad
 * @author netspider
 * @author hyungil.jeong
 *
 * @deprecated As of 1.9.0, application type detection timing has been changed to plugins' setup time.
 *             {@code ApplicationServerTypeResolver} should no longer be needed.
 */
@Deprecated
public class ApplicationServerTypeResolver {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ServiceType defaultType;
    private final ApplicationServerTypePluginResolver resolver;


    public ApplicationServerTypeResolver(List<ApplicationTypeDetector> applicationTypeDetector, ServiceType defaultType, List<String> orderedDetectors) {
        if (applicationTypeDetector == null) {
            throw new NullPointerException("applicationTypeDetector");
        }

        if (isValidApplicationServerType(defaultType)) {
            this.defaultType = defaultType;
        } else {
            this.defaultType = ServiceType.UNDEFINED;
        }

        List<ApplicationTypeDetector> sortedDetectors = sortByOrder(orderedDetectors, applicationTypeDetector);

        this.resolver = new ApplicationServerTypePluginResolver(sortedDetectors);
    }

    private List<ApplicationTypeDetector> sortByOrder(List<String> orderedDetectors, List<ApplicationTypeDetector> applicationTypeDetectors) {
        final List<ApplicationTypeDetector> detectionOrder = new ArrayList<ApplicationTypeDetector>();

        Map<String, ApplicationTypeDetector> applicationTypeDetectorMap = toMap(applicationTypeDetectors);
        for (String orderedDetector : orderedDetectors) {
            if (applicationTypeDetectorMap.containsKey(orderedDetector)) {
                detectionOrder.add(applicationTypeDetectorMap.remove(orderedDetector));
            }
        }

        detectionOrder.addAll(applicationTypeDetectorMap.values());
        return detectionOrder;
    }

    private Map<String, ApplicationTypeDetector> toMap(List<ApplicationTypeDetector> applicationTypeDetectorList) {

        Map<String, ApplicationTypeDetector> typeDetectorMap = new HashMap<String, ApplicationTypeDetector>();
        for (ApplicationTypeDetector applicationTypeDetector : applicationTypeDetectorList) {
            typeDetectorMap.put(applicationTypeDetector.getClass().getName(), applicationTypeDetector);
        }
        return typeDetectorMap;
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
