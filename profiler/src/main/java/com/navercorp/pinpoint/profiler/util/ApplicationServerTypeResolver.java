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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.plugin.ApplicationServerProperty;
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
    private String[] serverLibPath;

    private final ServiceType defaultType;
    private final List<ServerTypeDetector> detectors = new ArrayList<ServerTypeDetector>();

    /**
     * If we have to invoke startup() during agent initialization.
     * Some service types like BLOC or STAND_ALONE don't have an acceptor to do this.
     */
    private boolean manuallyStartupRequired = true;
    
    public ApplicationServerTypeResolver(List<DefaultProfilerPluginContext> plugins, ServiceType defaultType) {
        this.defaultType = defaultType;
        
        for (DefaultProfilerPluginContext context : plugins) {
            detectors.addAll(context.getServerTypeDetectors());
        }
    }

    public ApplicationServerTypeResolver() {
        this.defaultType = null;
    }

    public String[] getServerLibPath() {
        return serverLibPath;
    }

    public ServiceType getServerType() {
        return serverType;
    }
    
    public boolean isManuallyStartupRequired() {
        return manuallyStartupRequired;
    }

    public boolean resolve() {
        
        for (ServerTypeDetector detector : detectors) {
            logger.debug("try resolve using {}", detector.getClass());
            
            if (detector.detect()) {
                this.serverType = detector.getServerType();
                this.serverLibPath = detector.getServerClassPath();
                this.manuallyStartupRequired = !detector.hasServerProperty(ApplicationServerProperty.MANAGE_PINPOINT_AGENT_LIFECYCLE);

                if (logger.isInfoEnabled()) {
                    logger.info("Configured applicationServerType [{}] by {}", serverType, detector.getClass().getName());
                }
                
                return true;
            }
        }
        
        String applicationHome = applicationHomePath();
                
        if (tomcatResolve(applicationHome)) {
            return initializeApplicationInfo(applicationHome, ServiceType.TOMCAT);
        }

        if (defaultType != null) {
            if (logger.isInfoEnabled()) {
                logger.info("Configured applicationServerType:{}", defaultType);
            }
            
            return initializeApplicationInfo(applicationHome, defaultType);
        }
        
        return initializeApplicationInfo(applicationHome, ServiceType.STAND_ALONE);
    }

    private String applicationHomePath() {
        String path = null;
        
        if (System.getProperty("catalina.home") != null) {
            path = System.getProperty("catalina.home");
        } else {
            path = System.getProperty("user.dir");
        }
        
        if (logger.isInfoEnabled()) {
            logger.info("Resolved ApplicationHome : {}", path);
        }
        
        return path;
    }

    private boolean tomcatResolve(String applicationHome) {
        boolean isTomcat = false;
        
        if (isFileExist(new File(applicationHome + "/lib/catalina.jar"))) {
            this.manuallyStartupRequired = false;
            isTomcat = true;
        }
        
        return isTomcat;
    }

    private boolean initializeApplicationInfo(String applicationHome, ServiceType serviceType) {
        if (applicationHome == null) {
            logger.warn("applicationHome is null");
            return false;
        }

        if (ServiceType.TOMCAT.equals(serviceType)) {
            this.serverLibPath = new String[] { applicationHome + "/lib/servlet-api.jar", applicationHome + "/lib/catalina.jar" };
        } else if (ServiceType.STAND_ALONE.equals(serviceType)) {
            this.serverLibPath = new String[] {};
        } else if (ServiceType.TEST_STAND_ALONE.equals(serviceType)) {
            this.serverLibPath = new String[] {};
        } else {
            logger.warn("Invalid Default ApplicationServiceType:{} ", defaultType);
            return false;
        }

        this.serverType = serviceType;

        logger.info("ApplicationServerType:{}, RequiredServerLibraryPath:{}", serverType, serverLibPath);

        return true;
    }

    private boolean isFileExist(File libFile) {
        final boolean found = libFile.exists(); // && libFile.isFile();
        if (found) {
            logger.debug("libFile found:{}", libFile);
        } else {
            logger.debug("libFile not found:{}", libFile);
        }
        return found;
    }
}
