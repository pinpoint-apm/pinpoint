/*
 *  Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.fastjson;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * The type Fastjson config.
 *
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2017/07/17
 */
public class FastjsonConfig {

    private final boolean profile;

    /**
     * Instantiates a new Fastjson config.
     *
     * @param config the config
     */
    public FastjsonConfig(ProfilerConfig config) {
        this.profile = config.readBoolean(FastjsonConstants.CONFIG, false);
    }

    /**
     * Is profile boolean.
     *
     * @return the boolean
     */
    public boolean isProfile() {
        return profile;
    }

    @Override
    public String toString() {
        return "FastjsonConfig{" + "profile=" + profile + '}';
    }
}
