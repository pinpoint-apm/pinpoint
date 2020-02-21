/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.config;

import com.navercorp.pinpoint.bootstrap.agentdir.Assert;
import com.navercorp.pinpoint.common.util.SimpleProperty;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PropertyLoaderFactory {

    private final SimpleProperty systemProperty;
    private final String agentRootPath;

    // @Optional
    private final String profilesPath;
    // @Optional
    private final String[] supportedProfiles;

    public PropertyLoaderFactory(SimpleProperty systemProperty, String agentRootPath, String profilesPath, String[] supportedProfiles) {
        this.systemProperty = Assert.requireNonNull(systemProperty, "systemProperty");
        this.agentRootPath = Assert.requireNonNull(agentRootPath, "agentRootPath");
        this.profilesPath = Assert.requireNonNull(profilesPath, "profilesPath");
        this.supportedProfiles = Assert.requireNonNull(supportedProfiles, "supportedProfiles");
    }

    public PropertyLoader newPropertyLoader() {
        if (isSimpleMode()) {
            return new SimplePropertyLoader(systemProperty, agentRootPath, profilesPath);
        }
        return new ProfilePropertyLoader(systemProperty, agentRootPath, profilesPath, supportedProfiles);
    }



    private boolean isSimpleMode() {
        final String mode = systemProperty.getProperty(Profiles.CONFIG_LOAD_MODE_KEY, Profiles.CONFIG_LOAD_MODE.PROFILE.toString());
        return Profiles.CONFIG_LOAD_MODE.SIMPLE.toString().equalsIgnoreCase(mode);
    }
}
