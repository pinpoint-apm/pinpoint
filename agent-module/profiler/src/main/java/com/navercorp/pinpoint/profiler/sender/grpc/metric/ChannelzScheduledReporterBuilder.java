/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.sender.grpc.metric;

import com.google.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.time.Duration;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class ChannelzScheduledReporterBuilder {

    private Duration reportPeriod = Duration.ofSeconds(60);

    public ChannelzScheduledReporterBuilder acceptConfig(ProfilerConfig config) {
        String reportPeriodStr = Objects.requireNonNull(config, "config").getGrpcStatLoggingPeriod();
        this.reportPeriod = Duration.parse(reportPeriodStr);
        return this;
    }

    @VisibleForTesting
    ChannelzScheduledReporterBuilder setReportPeriod(Duration reportPeriod) {
        this.reportPeriod = reportPeriod;
        return this;
    }

    public ChannelzScheduledReporter build() {
        if (this.reportPeriod.toMillis() <= 0) {
            return new EmptyChannelzScheduledReporter();
        } else {
            return new DefaultChannelzScheduledReporter(this.reportPeriod);
        }
    }

}
