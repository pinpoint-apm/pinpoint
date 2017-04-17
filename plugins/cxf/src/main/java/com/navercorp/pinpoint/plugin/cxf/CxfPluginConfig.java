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
package com.navercorp.pinpoint.plugin.cxf;

import java.util.Arrays;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * @author barney
 *
 */
public class CxfPluginConfig {

    private final boolean clientProfile;

    private final String[] clientHiddenParams;

    public CxfPluginConfig(ProfilerConfig src) {
        this.clientProfile = src.readBoolean("profiler.cxf.client", false);
        this.clientHiddenParams = getStringArray(src.readString("profiler.cxf.client.hiddenParams", ""));
    }

    public boolean isClientProfile() {
        return clientProfile;
    }

    public String[] getClientHiddenParams() {
        return clientHiddenParams;
    }

    private String[] getStringArray(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        String[] split = value.split(",");
        String[] array = new String[split.length];
        for (int i = 0; i < split.length; i++) {
            array[i] = split[i].trim();
        }
        return array;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CxfPluginConfig [clientProfile=");
        builder.append(clientProfile);
        builder.append(", clientHiddenParams=");
        builder.append(Arrays.toString(clientHiddenParams));
        builder.append("]");
        return builder.toString();
    }
}
