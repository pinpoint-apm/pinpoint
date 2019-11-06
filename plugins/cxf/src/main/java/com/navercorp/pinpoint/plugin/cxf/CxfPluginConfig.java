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
package com.navercorp.pinpoint.plugin.cxf;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author barney
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2017/10/03
 */
public class CxfPluginConfig {

    private final boolean serviceProfile;

    private final boolean loggingProfile;
    @Deprecated
    private final boolean clientProfile;
    @Deprecated
    private final String[] clientHiddenParams;


    /**
     * Instantiates a new Cxf plugin config.
     * <p>
     * profiler.cxf.client and profiler.cxf.client.hiddenParams is deprecated.
     *
     * @param src the src
     */
    public CxfPluginConfig(ProfilerConfig src) {
        this.serviceProfile = src.readBoolean("profiler.cxf.service.enable", false);
        this.loggingProfile = src.readBoolean("profiler.cxf.logging.enable", false);
        this.clientProfile = src.readBoolean("profiler.cxf.client", false);
        this.clientHiddenParams = getStringArray(src.readString("profiler.cxf.client.hiddenParams", ""));
    }

    public boolean isServiceProfile() {
        return serviceProfile;
    }

    public boolean isLoggingProfile() {
        return loggingProfile;
    }

    @Deprecated
    public boolean isClientProfile() {
        return clientProfile;
    }

    @Deprecated
    public String[] getClientHiddenParams() {
        return clientHiddenParams;
    }

    @Deprecated
    private String[] getStringArray(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        List<String> tokenList = StringUtils.tokenizeToStringList(value, ",");
        return toStringArray(tokenList);
    }

    @Deprecated
    private String[] toStringArray(List<String> list) {
        if (list == null) {
            return null;
        }

        return list.toArray(new String[0]);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CxfPluginConfig [serviceProfile=");
        builder.append(serviceProfile);
        builder.append(", loggingProfile=");
        builder.append(loggingProfile);
        builder.append(", clientProfile(Deprecated)=");
        builder.append(clientProfile);
        builder.append(", clientHiddenParams(Deprecated)=");
        builder.append(Arrays.toString(clientHiddenParams));
        builder.append("]");
        return builder.toString();
    }
}