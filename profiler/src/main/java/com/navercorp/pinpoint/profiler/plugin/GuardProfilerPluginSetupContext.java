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
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.uri.UriExtractorProvider;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author emeroad
 */
public class GuardProfilerPluginSetupContext implements ProfilerPluginSetupContext {

    private final ProfilerPluginSetupContext delegate;
    private boolean close = false;

    public GuardProfilerPluginSetupContext(ProfilerPluginSetupContext delegate) {
        if (delegate == null) {
            throw new NullPointerException("delegate");
        }
        this.delegate = delegate;
    }

    @Override
    public ProfilerConfig getConfig() {
//        checkOpen();
        return this.delegate.getConfig();
    }

    @Override
    public void addApplicationTypeDetector(ApplicationTypeDetector... detectors) {
        checkOpen();
        this.delegate.addApplicationTypeDetector(detectors);
    }

    @Override
    public ServiceType getConfiguredApplicationType() {
        return delegate.getConfiguredApplicationType();
    }

    @Override
    public ServiceType getApplicationType() {
        return delegate.getApplicationType();
    }

    @Override
    public boolean registerApplicationType(ServiceType applicationType) {
        checkOpen();
        return this.delegate.registerApplicationType(applicationType);
    }

    @Override
    public void addJdbcUrlParser(JdbcUrlParserV2 jdbcUrlParser) {
        checkOpen();
        this.delegate.addJdbcUrlParser(jdbcUrlParser);
    }

    @Override
    public void addUriExtractor(UriExtractorProvider uriExtractorProvider) {
        checkOpen();
        this.delegate.addUriExtractor(uriExtractorProvider);
    }

    private void checkOpen() {
        if (close) {
            throw new IllegalStateException("ProfilerPluginSetupContext already initialized");
        }
    }

    public void close() {
        this.close = true;
    }
}
