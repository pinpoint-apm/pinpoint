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
package com.navercorp.pinpoint.plugin.druid;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * The type Druid config.
 *
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2017/07/21
 */
public class DruidConfig {

    private final boolean pluginEnable;
    private final boolean profileClose;

    /**
     * Instantiates a new Druid config.
     *
     * @param config the config
     */
    public DruidConfig(ProfilerConfig config) {

        pluginEnable = config.readBoolean(DruidConstants.PLUGIN_ENABLE, false);
        profileClose = config.readBoolean(DruidConstants.PROFILE_CONNECTIONCLOSE_ENABLE, false);
    }

    /**
     * Is plugin enable boolean.
     *
     * @return the boolean
     */
    public boolean isPluginEnable() {
        return pluginEnable;
    }

    /**
     * Is profile close boolean.
     *
     * @return the boolean
     */
    public boolean isProfileClose() {
        return profileClose;
    }

    @Override
    public String toString() {
        return "DruidConfig{" +
                "pluginEnable=" + pluginEnable +
                ", profileClose=" + profileClose +
                '}';
    }
}