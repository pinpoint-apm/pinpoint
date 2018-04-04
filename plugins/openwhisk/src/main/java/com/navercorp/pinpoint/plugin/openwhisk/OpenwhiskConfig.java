/*
 * Copyright 2018 NAVER Corp.
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


public class OpenwhiskConfig {

    /**
     * openwhisk
     */
    private boolean enable = true;

    private String caller;

    public OpenwhiskConfig(ProfilerConfig config) {
        /*
         * openwhisk
         */
        this.enable = config.readBoolean("profiler.openwhisk.enable", false);
        this.caller = config.readString("profiler.openwhisk.caller", "CALLER");
    }

    public boolean isEnable() {
        return enable;
    }

    public String getCaller() {
        return caller;
    }
}
