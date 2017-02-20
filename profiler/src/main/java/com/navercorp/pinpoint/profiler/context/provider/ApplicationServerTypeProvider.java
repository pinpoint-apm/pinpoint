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
import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.plugin.PluginContextLoadResult;
import com.navercorp.pinpoint.profiler.util.ApplicationServerTypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * @author Woonduk Kang(emeroad)
 */
public class ApplicationServerTypeProvider implements Provider<ServiceType> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ProfilerConfig profilerConfig;
    private final ServiceTypeRegistryService serviceTypeRegistryService;
    private final Provider<PluginContextLoadResult> pluginContextLoadResultProvider;

    @Inject
    public ApplicationServerTypeProvider(ProfilerConfig profilerConfig, ServiceTypeRegistryService serviceTypeRegistryService, Provider<PluginContextLoadResult> pluginContextLoadResultProvider) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (serviceTypeRegistryService == null) {
            throw new NullPointerException("serviceTypeRegistryService must not be null");
        }
        this.profilerConfig = profilerConfig;
        this.serviceTypeRegistryService = serviceTypeRegistryService;
        this.pluginContextLoadResultProvider = pluginContextLoadResultProvider;
    }

    @Override
    public ServiceType get() {
        final ServiceType applicationServiceType = getApplicationServiceType();
        logger.info("default ApplicationServerType={}", applicationServiceType);

        PluginContextLoadResult pluginContextLoadResult = this.pluginContextLoadResultProvider.get();
        List<ApplicationTypeDetector> applicationTypeDetectorList = pluginContextLoadResult.getApplicationTypeDetectorList();
        ApplicationServerTypeResolver applicationServerTypeResolver = new ApplicationServerTypeResolver(applicationTypeDetectorList, applicationServiceType, profilerConfig.getApplicationTypeDetectOrder());
        ServiceType resolve = applicationServerTypeResolver.resolve();
        logger.info("resolved ApplicationServerType={}", resolve);
        return resolve;
    }

    private ServiceType getApplicationServiceType() {
        String applicationServerTypeString = profilerConfig.getApplicationServerType();
        return this.serviceTypeRegistryService.findServiceTypeByName(applicationServerTypeString);
    }
}
