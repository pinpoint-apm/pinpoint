/*
 *  Copyright 2016 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.plugin.json_lib;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author Taejin Koo
 */
public class JsonLibConfig {

    private final boolean profile;

    public JsonLibConfig(ProfilerConfig config) {
        this.profile = config.readBoolean("profiler.json.jsonlib", false);
    }

    public boolean isProfile() {
        return profile;
    }

    @Override
    public String toString() {
        return "JsonLibConfig{" + "profile=" + profile + '}';
    }

}
