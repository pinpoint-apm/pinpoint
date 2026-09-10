/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.log.collector;

import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

/**
 * The log receiver's own tuning (admission budgets, worker pool, filter blacklist), kept apart from
 * the trace collector's files so the two signals are sized independently. The profile file overrides
 * the root file. The module gate itself ({@link OtlpLogCollectorModule#ENABLED_KEY}) is evaluated
 * before these sources are imported, so it must come from the starter configuration or external
 * config, not from here.
 */
@PropertySources({
        @PropertySource(name = "OtlpLogCollectorPropertySources", value = {
                OtlpLogCollectorPropertySources.ROOT,
                OtlpLogCollectorPropertySources.PROFILE
        })
})
public final class OtlpLogCollectorPropertySources {

    public static final String ROOT = "classpath:otlplog/collector/pinpoint-otlplog-root.properties";
    public static final String PROFILE = "classpath:otlplog/collector/profiles/${pinpoint.profiles.active:local}/pinpoint-otlplog.properties";

    private OtlpLogCollectorPropertySources() {
    }
}
