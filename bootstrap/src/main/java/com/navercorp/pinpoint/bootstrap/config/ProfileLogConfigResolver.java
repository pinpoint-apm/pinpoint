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

import java.io.File;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ProfileLogConfigResolver implements LogConfigResolver {
    private final String profilesPath;
    private final String activeProfile;

    public ProfileLogConfigResolver(String profilesPath, String activeProfile) {
        this.profilesPath = Assert.requireNonNull(profilesPath, "profilesPath");
        this.activeProfile = Assert.requireNonNull(activeProfile, "activeProfile");
    }

    @Override
    public String getLogPath() {
        return profilesPath + File.separator + activeProfile + File.separator + "log4j.xml";
    }
}
