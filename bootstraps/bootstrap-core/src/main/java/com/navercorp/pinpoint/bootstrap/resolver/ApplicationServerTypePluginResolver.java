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

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ApplicationTypeDetector;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * This class attempts to resolve the current application type through {@link ApplicationTypeDetector}s.
 * The application type is resolved by checking the conditions defined in each of the loaded detector's {@code detect} method.
 * <p>
 * If no match is found, the application type defaults to {@code ServiceType.STAND_ALONE}
 * 
 * @author HyunGil Jeong
 *
 * @deprecated As of 1.9.0, application type detection timing has been changed to plugins' setup time.
 *             {@code ApplicationServerTypePluginResolver} should no longer be needed.
 */
@Deprecated
public class ApplicationServerTypePluginResolver {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private final List<ApplicationTypeDetector> applicationTypeDetectors;
    
    private final ConditionProvider conditionProvider;
    
    private static final ServiceType DEFAULT_SERVER_TYPE = ServiceType.STAND_ALONE;
    
    public ApplicationServerTypePluginResolver(List<ApplicationTypeDetector> serverTypeDetectors) {
        this(serverTypeDetectors, ConditionProvider.DEFAULT_CONDITION_PROVIDER);
    }
    
    public ApplicationServerTypePluginResolver(List<ApplicationTypeDetector> serverTypeDetectors, ConditionProvider conditionProvider) {
        if (serverTypeDetectors == null) {
            throw new IllegalArgumentException("applicationTypeDetectors should not be null");
        }
        if (conditionProvider == null) {
            throw new IllegalArgumentException("conditionProvider should not be null");
        }
        this.applicationTypeDetectors = serverTypeDetectors;
        this.conditionProvider = conditionProvider;
    }

    public ServiceType resolve() {
        for (ApplicationTypeDetector currentDetector : this.applicationTypeDetectors) {
            String currentDetectorName = currentDetector.getClass().getName();
            logger.info("Attempting to resolve using [{}]", currentDetectorName);
            if (currentDetector.detect(this.conditionProvider)) {
                logger.info("Match found using [{}]", currentDetectorName);
                return currentDetector.getApplicationType();
            } else {
                logger.info("No match found using [{}]", currentDetectorName);
            }
        }
        logger.debug("Server type not resolved. Defaulting to {}", DEFAULT_SERVER_TYPE.getName());
        return DEFAULT_SERVER_TYPE;
    }
}
