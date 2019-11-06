/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.bootstrap.plugin;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 *  Provides attributes and objects to interceptors.
 * 
 *  Only interceptors can acquire an instance of this class as a constructor argument.
 * 
 * @author Jongho Moon
 *
 */
public interface ProfilerPluginSetupContext {
    /**
     * Get the {@link ProfilerConfig}
     * 
     * @return {@link ProfilerConfig}
     */
    ProfilerConfig getConfig();

    /**
     * Add a {@link ApplicationTypeDetector} to Pinpoint agent.
     *
     * @param detectors application type detectors to add
     *
     * @deprecated As of 1.9.0, {@code ApplicationTypeDetector} has been deprecated.
     *             Use {@link #registerApplicationType(ServiceType)} instead.
     */
    @Deprecated
    void addApplicationTypeDetector(ApplicationTypeDetector... detectors);

    /**
     * Returns the {@link ServiceType} configured by <tt>profiler.applicationservertype</tt>
     * in <i>pinpoint.config</i> file.
     *
     * @return the configured {@link ServiceType}
     */
    ServiceType getConfiguredApplicationType();

    /**
     * Returns the {@link ServiceType} registered by plugins.
     *
     * @return the registered {@link ServiceType}
     */
    ServiceType getApplicationType();

    /**
     * Registers the specified {@link ServiceType} to be the application type of the agent.
     * Returns <tt>false</tt> if the application type has already been registered, and the
     * supplied <tt>applicationType</tt> should not be registered.
     * <p>
     * The <tt>profiler.plugin.load.order</tt> option in <i>pinpoint.config</i> may be used
     * to configure the order in which the registration happens.
     *
     * @param applicationType the applicationt type to be registered
     * @return <tt>true</tt> if the application type is registered
     */
    boolean registerApplicationType(ServiceType applicationType);

    void addJdbcUrlParser(JdbcUrlParserV2 jdbcUrlParserV2);

}
