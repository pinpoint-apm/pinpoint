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

package com.navercorp.pinpoint.profiler.plugin;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.plugin.ApplicationTypeDetector;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginGlobalContext;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.Assert;

import java.util.ArrayList;
import java.util.List;


/**
 * @author jaehong.kim
 */
public class DefaultProfilerPluginSetupContext implements ProfilerPluginSetupContext {

    private final ProfilerPluginGlobalContext globalContext;

    private final List<ApplicationTypeDetector> serverTypeDetectors = new ArrayList<ApplicationTypeDetector>();
    private final List<JdbcUrlParserV2> jdbcUrlParserList = new ArrayList<JdbcUrlParserV2>();

    public DefaultProfilerPluginSetupContext(ProfilerPluginGlobalContext globalContext) {
        this.globalContext = Assert.requireNonNull(globalContext, "globalContext");
    }

    @Override
    public ProfilerConfig getConfig() {
        return globalContext.getConfig();
    }

    @Override
    public void addApplicationTypeDetector(ApplicationTypeDetector... detectors) {
        if (detectors == null) {
            return;
        }
        for (ApplicationTypeDetector detector : detectors) {
            serverTypeDetectors.add(detector);
        }
    }

    public List<ApplicationTypeDetector> getApplicationTypeDetectors() {
        return serverTypeDetectors;
    }

    @Override
    public ServiceType getConfiguredApplicationType() {
        return globalContext.getConfiguredApplicationType();
    }

    @Override
    public ServiceType getApplicationType() {
        return globalContext.getApplicationType();
    }

    @Override
    public boolean registerApplicationType(ServiceType applicationType) {
        return globalContext.registerApplicationType(applicationType);
    }

    @Override
    public void addJdbcUrlParser(JdbcUrlParserV2 jdbcUrlParser) {
        if (jdbcUrlParser == null) {
            return;
        }

        this.jdbcUrlParserList.add(jdbcUrlParser);
    }

    public List<JdbcUrlParserV2> getJdbcUrlParserList() {
        return jdbcUrlParserList;
    }

}
