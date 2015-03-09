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
import java.util.List;

import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.plugin.ServerTypeDetector;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.profiler.plugin.DefaultProfilerPluginContext;

/**
 * @author emeroad
 * @author netspider
 */
public class ApplicationServerTypeResolver {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ServiceType serverType;

    private final ServiceType defaultType;
    private final List<ServerTypeDetector> detectors = new ArrayList<ServerTypeDetector>();

    private final ServiceTypeRegistryService serviceTypeRegistryService;
    /**
     * If we have to invoke startup() during agent initialization.
     * Some service types like BLOC or STAND_ALONE don't have an acceptor to do this.
     */
    private boolean manuallyStartupRequired = true;

    public ApplicationServerTypeResolver(List<DefaultProfilerPluginContext> plugins, ServiceType defaultType, ServiceTypeRegistryService serviceTypeRegistryService) {
        if (serviceTypeRegistryService == null) {
            throw new NullPointerException("serviceTypeRegistryService must not be null");
        }

        this.defaultType = defaultType;
        
        for (DefaultProfilerPluginContext context : plugins) {
            detectors.addAll(context.getServerTypeDetectors());
        }

        this.serviceTypeRegistryService = serviceTypeRegistryService;
    }


    public String[] getServerLibPath() {
        return new String[0];
    }

    public ServiceType getServerType() {
        return serverType;
    }
    
    public boolean isManuallyStartupRequired() {
        return manuallyStartupRequired;
    }
    
    public boolean resolve() {
        String serverType = null;

        for (ServerTypeDetector detector : detectors) {
            logger.debug("try to resolve using {}", detector.getClass());
            
            if (serverType != null && !detector.canOverride(serverType)) {
                continue;
            }
            
            if (detector.detect()) {
                serverType = detector.getServerTypeName();

                if (logger.isInfoEnabled()) {
                    logger.info("Resolved applicationServerType [{}] by {}", serverType, detector.getClass().getName());
                }
            }
        }
        
        if (serverType != null) {
            this.serverType = serviceTypeRegistryService.findServiceTypeByName(serverType);
            return true;
        }
        
        if (defaultType != null) {
            // TODO validate default type. is defaultType a server type?
            this.serverType = defaultType;
        } else {
            this.serverType = ServiceType.STAND_ALONE;
        }
        
        if (logger.isInfoEnabled()) {
            logger.info("Configured applicationServerType:{}", defaultType);
        }
        
        return true;
    }
}
