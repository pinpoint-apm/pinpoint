/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.plugin.ApplicationTypeDetector;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.module.ConfiguredApplicationType;
import com.navercorp.pinpoint.profiler.plugin.PluginContextLoadResult;
import com.navercorp.pinpoint.profiler.util.ApplicationServerTypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ApplicationServerTypeProvider implements Provider<ServiceType> {

    private static final ServiceType DEFAULT_APPLICATION_TYPE = ServiceType.STAND_ALONE;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ProfilerConfig profilerConfig;
    private final ServiceType configuredApplicationType;
    private final Provider<PluginContextLoadResult> pluginContextLoadResultProvider;

    @Inject
    public ApplicationServerTypeProvider(ProfilerConfig profilerConfig, @ConfiguredApplicationType ServiceType configuredApplicationType, Provider<PluginContextLoadResult> pluginContextLoadResultProvider) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig");
        this.configuredApplicationType = Assert.requireNonNull(configuredApplicationType, "configuredApplicationType");
        this.pluginContextLoadResultProvider = pluginContextLoadResultProvider;
    }

    @Override
    public ServiceType get() {
        if (configuredApplicationType != ServiceType.UNDEFINED) {
            logger.info("Configured ApplicationServerType={}", configuredApplicationType);
            return configuredApplicationType;
        }

        PluginContextLoadResult pluginContextLoadResult = this.pluginContextLoadResultProvider.get();
        ServiceType resolvedApplicationType = pluginContextLoadResult.getApplicationType();
        if (resolvedApplicationType == null) {

            // FIXME remove block when ApplicationTypeDetector is removed.
            List<ApplicationTypeDetector> detectors = pluginContextLoadResult.getApplicationTypeDetectorList();
            if (!detectors.isEmpty()) {
                return resolveUsingRegisteredDetectors(detectors, configuredApplicationType, profilerConfig.getApplicationTypeDetectOrder());
            }

            logger.info("Application type not resolved. Defaulting to {}", DEFAULT_APPLICATION_TYPE);
            return DEFAULT_APPLICATION_TYPE;
        }
        logger.info("Resolved Application type : {}", resolvedApplicationType);
        return resolvedApplicationType;
    }

    private ServiceType resolveUsingRegisteredDetectors(List<ApplicationTypeDetector> applicationTypeDetectorList,
                                                        ServiceType applicationServiceType,
                                                        List<String> applicationTypeDetectOrder) {
        ApplicationServerTypeResolver applicationServerTypeResolver = new ApplicationServerTypeResolver(
                applicationTypeDetectorList, applicationServiceType, applicationTypeDetectOrder);
        ServiceType resolve = applicationServerTypeResolver.resolve();
        return resolve;
    }
}
