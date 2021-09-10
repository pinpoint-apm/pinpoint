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

import java.util.Objects;

import com.navercorp.pinpoint.profiler.context.monitor.config.MonitorConfig;
import com.navercorp.pinpoint.profiler.context.storage.AsyncQueueingUriStatStorage;
import com.navercorp.pinpoint.profiler.context.storage.DisabledUriStatStorage;
import com.navercorp.pinpoint.profiler.context.storage.UriStatStorage;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author Taejin Koo
 */
public class UriStatStorageProvider implements Provider<UriStatStorage> {

    private static final String URI_STAT_STORAGE_EXECUTOR_NAME = "Pinpoint-StatStorageExecutor";

    private final MonitorConfig monitorConfig;

    @Inject
    public UriStatStorageProvider(MonitorConfig monitorConfig) {
        this.monitorConfig = Objects.requireNonNull(monitorConfig, "monitorConfig");
    }

    @Override
    public UriStatStorage get() {
        if (monitorConfig.isUriStatEnable()) {
            return new AsyncQueueingUriStatStorage(5192, monitorConfig.getCompletedUriStatDataLimitSize(), URI_STAT_STORAGE_EXECUTOR_NAME);
        } else {
            return new DisabledUriStatStorage();
        }
    }
}