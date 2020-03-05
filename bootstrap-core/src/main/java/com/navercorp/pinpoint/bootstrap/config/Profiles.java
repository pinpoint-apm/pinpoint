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

/**
 * @author Woonduk Kang(emeroad)
 */
public final class Profiles {
    private Profiles() {
    }

    public static final String LOG_CONFIG_LOCATION_KEY = "pinpoint.profiler.log.config.location";

    public static final String CONFIG_LOAD_MODE_KEY = "pinpoint.config.load.mode";
    public enum CONFIG_LOAD_MODE {
        PROFILE,
        // for IT TEST
        SIMPLE
    }

    public static final String ACTIVE_PROFILE_KEY = "pinpoint.profiler.profiles.active";
    public static final String DEFAULT_ACTIVE_PROFILE = "release";

    // 1. default config
    public static final String CONFIG_FILE_NAME = "pinpoint.config";
    // 2. profile config
    public static final String PROFILE_CONFIG_FILE_NAME = "pinpoint-env.config";
    // 3. external config
    public static final String EXTERNAL_CONFIG_KEY = CONFIG_FILE_NAME;
}
