/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.aws.sdk.s3;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.util.Objects;

public class AwsSdkS3PluginConfig {
    private final boolean enable;
    private boolean statusCode = true;
    private boolean markError;

    public static boolean isStatusCode(ProfilerConfig config) {
        return config.readBoolean("profiler.aws.sdk.s3.statuscode", Boolean.TRUE);
    }

    public static boolean isMarkError(ProfilerConfig config) {
        return config.readBoolean("profiler.aws.sdk.s3.mark.error", Boolean.TRUE);
    }

    public AwsSdkS3PluginConfig(ProfilerConfig config) {
        Objects.requireNonNull(config, "config");

        // plugin
        this.enable = config.readBoolean("profiler.aws.sdk.s3.enable", true);
        this.statusCode = isStatusCode(config);
        this.markError = isMarkError(config);
    }

    public boolean isEnable() {
        return enable;
    }

    @Override
    public String toString() {
        return "AwsSdkS3PluginConfig{" +
                "enable=" + enable +
                ", statusCode=" + statusCode +
                ", markError=" + markError +
                '}';
    }
}