/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.tx;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

public class SpringTxConfig {
    private final boolean enabled;
    private final boolean markError;

    public SpringTxConfig(ProfilerConfig config) {
        this.enabled = config.readBoolean("profiler.spring.tx.enable", true);
        this.markError = config.readBoolean("profiler.spring.tx.mark.error", false);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isMarkError() {
        return markError;
    }

    @Override
    public String toString() {
        return "SpringTxConfig{" +
                "enabled=" + enabled +
                ", markError=" + markError +
                '}';
    }
}
