/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.storage.DisabledUriStatStorage;
import com.navercorp.pinpoint.profiler.context.storage.UriStatStorage;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author Taejin Koo
 */
public class UriStatStorageProvider implements Provider<UriStatStorage> {

    private final ProfilerConfig profilerConfig;

    @Inject
    public UriStatStorageProvider(ProfilerConfig profilerConfig) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig");
    }

    @Override
    public UriStatStorage get() {
        if (profilerConfig.isUriStatEnable()) {
            // TO DO : have to change
            return new DisabledUriStatStorage();
        } else {
            return new DisabledUriStatStorage();
        }
    }
}