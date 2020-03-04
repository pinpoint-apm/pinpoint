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
package com.navercorp.pinpoint.plugin.openwhisk;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.util.List;

/**
 * @author Seonghyun Oh
 */
public class OpenwhiskConfig {

    private final boolean enable;

    private final boolean loggingMessage;

    private final String transformTargetName;
    private final List<String> transformParameters;

    static final String KEY_TRANSFORM_TARGET_NAME = "profiler.openwhisk.transform.targetname";
    static final String KEY_TRANSFORM_PARAMETERS = "profiler.openwhisk.transform.targetparameter";

    private static final String DEFAULT_TRANSFORM_TARGET_NAME = "org.apache.openwhisk.http.BasicHttpService.$anonfun$assignId$2";

    public OpenwhiskConfig(ProfilerConfig config) {
        /*
         * openwhisk
         */
        this.enable = config.readBoolean("profiler.openwhisk.enable", false);
        this.loggingMessage = config.readBoolean("profiler.openwhisk.logging.message", false);

        this.transformTargetName = config.readString(KEY_TRANSFORM_TARGET_NAME, DEFAULT_TRANSFORM_TARGET_NAME);
        this.transformParameters = config.readList(KEY_TRANSFORM_PARAMETERS);
    }

    public String getTransformTargetName() {
        return transformTargetName;
    }

    public List<String> getTransformTargetParameters() { return transformParameters; }

    public boolean isEnable() {
        return enable;
    }

    public boolean isLoggingMessage() {
        return loggingMessage;
    }

    @Override
    public String toString() {
        return "OpenwhiskConfig{" +
                "enable=" + enable +
                ", loggingMessage=" + loggingMessage +
                ", transformTargetName='" + transformTargetName + '\'' +
                '}';
    }
}
