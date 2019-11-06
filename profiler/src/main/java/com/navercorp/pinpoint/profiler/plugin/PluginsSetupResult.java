/*
 * Copyright 2018 NAVER Corp.
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

import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class PluginsSetupResult {

    private final List<PluginSetupResult> pluginSetupResults = new ArrayList<PluginSetupResult>();
    private ServiceType applicationType;

    public void addPluginSetupResult(PluginSetupResult pluginSetupResult) {
        if (pluginSetupResult == null) {
            return;
        }
        this.pluginSetupResults.add(pluginSetupResult);
    }

    public void addPluginSetupResults(Collection<PluginSetupResult> pluginSetupResults) {
        if (pluginSetupResults == null) {
            return;
        }
        this.pluginSetupResults.addAll(pluginSetupResults);
    }

    public List<PluginSetupResult> getPluginSetupResults() {
        return Collections.unmodifiableList(pluginSetupResults);
    }

    public ServiceType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ServiceType applicationType) {
        this.applicationType = applicationType;
    }
}
