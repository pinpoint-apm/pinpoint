/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.resolver;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.navercorp.pinpoint.bootstrap.plugin.ServerTypeDetector;
import com.navercorp.pinpoint.common.ServiceType;

/**
 * This class attempts to resolve the current application type through {@link ServerTypeDetector}s.
 * The application type is resolved by checking the conditions defined in each of the loaded detector's {@code detect} method.
 * <p>
 * If no match is found, the application type defaults to {@code ServiceType.STAND_ALONE}
 * 
 * @author HyunGil Jeong
 */
public class ApplicationServerTypePluginResolver {

    private final Logger logger = Logger.getLogger(ApplicationServerTypePluginResolver.class.getName());

    private final List<ServerTypeDetector> serverTypeDetectors;
    
    private final ConditionProvider conditionProvider;
    
    private static final ServiceType DEFAULT_SERVER_TYPE = ServiceType.STAND_ALONE;
    
    public ApplicationServerTypePluginResolver(List<ServerTypeDetector> serverTypeDetectors) {
        this(serverTypeDetectors, ConditionProvider.DEFAULT_CONDITION_PROVIDER);
    }
    
    public ApplicationServerTypePluginResolver(List<ServerTypeDetector> serverTypeDetectors, ConditionProvider conditionProvider) {
        if (serverTypeDetectors == null) {
            throw new IllegalArgumentException("serverTypeDetectors should not be null");
        }
        if (conditionProvider == null) {
            throw new IllegalArgumentException("conditionProvider should not be null");
        }
        this.serverTypeDetectors = serverTypeDetectors;
        this.conditionProvider = conditionProvider;
    }

    public ServiceType resolve() {
        for (ServerTypeDetector currentDetector : this.serverTypeDetectors) {
            logger.log(Level.INFO, "Attempting to resolve using " + currentDetector.getClass());
            if (currentDetector.detect(this.conditionProvider)) {
                logger.log(Level.INFO, "Match found using " + currentDetector.getClass().getSimpleName());
                return currentDetector.getServerType();
            } else {
                logger.log(Level.INFO, "No match found using " + currentDetector.getClass());
            }
        }
        logger.log(Level.INFO, "Server type not resolved. Defaulting to " + DEFAULT_SERVER_TYPE.getName());
        return DEFAULT_SERVER_TYPE;
    }
}
