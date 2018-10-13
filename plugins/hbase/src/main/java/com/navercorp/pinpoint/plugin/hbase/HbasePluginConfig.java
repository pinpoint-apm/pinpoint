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
package com.navercorp.pinpoint.plugin.hbase;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * The type Hbase plugin config.
 *
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2018/10/12
 */
public class HbasePluginConfig {

    private final boolean hbaseProfile;
    private final boolean operationProfile;

    /**
     * Instantiates a new Hbase plugin config.
     *
     * @param config the config
     */
    public HbasePluginConfig(ProfilerConfig config) {
        this.hbaseProfile = config.readBoolean(HbasePluginConstants.HBASE_CONFIG, true);
        this.operationProfile = config.readBoolean(HbasePluginConstants.HBASE_OPS_CONFIG, true);
    }

    /**
     * Is hbase profile boolean.
     *
     * @return the boolean
     */
    public boolean isHbaseProfile() {
        return hbaseProfile;
    }

    /**
     * Is operation profile boolean.
     *
     * @return the boolean
     */
    public boolean isOperationProfile() {
        return operationProfile;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("HbasePluginConfig [hbaseProfile=");
        builder.append(hbaseProfile);
        builder.append(", operationProfile=");
        builder.append(operationProfile);
        builder.append("]");
        return builder.toString();
    }
}
