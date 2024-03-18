/*
 *  Copyright 2019 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.elasticsearch;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author Roy Kim
 */
public class ElasticsearchPluginConfig {

    private final boolean elasticsearchEnabled;
    private final boolean recordDsl;
    private final boolean recordESVersion;

    public ElasticsearchPluginConfig(ProfilerConfig profilerConfig) {
        if (profilerConfig != null) {
            this.elasticsearchEnabled = profilerConfig.readBoolean("profiler.elasticsearch.enabled", true);
            this.recordDsl = profilerConfig.readBoolean("profiler.elasticsearch.recordDsl", true);
            this.recordESVersion = profilerConfig.readBoolean("profiler.elasticsearch.recordESVersion", false);
        } else {
            this.elasticsearchEnabled = true;
            this.recordDsl = true;
            this.recordESVersion = false;
        }
    }

    public boolean isEnabled() {
        return elasticsearchEnabled;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ElasticsearchPluginConfig{");
        sb.append("ElasticsearchEnabled=").append(elasticsearchEnabled);
        sb.append(",recordDsl=").append(recordDsl);
        sb.append(",recordESVersion=").append(recordESVersion);
        sb.append('}');
        return sb.toString();
    }

    public boolean isRecordDsl() {
        return recordDsl;
    }

    public boolean isRecordESVersion() {
        return recordESVersion;
    }

}
