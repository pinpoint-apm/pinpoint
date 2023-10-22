/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.sender.grpc.metric;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author youngjin.kim2
 */
public class ChannelzScheduledReporterBuilderTest {

    @Test
    public void shouldReturnEmptyWhenDurationIsZero() {
        ChannelzScheduledReporter empty = new ChannelzScheduledReporterBuilder()
                .setReportPeriod(Duration.ZERO)
                .build();
        assertThat(empty).isInstanceOf(EmptyChannelzScheduledReporter.class);

        Duration zeroDuration = Duration.ofMillis(0);
        ChannelzScheduledReporter empty2 = new ChannelzScheduledReporterBuilder()
                .setReportPeriod(zeroDuration)
                .build();
        assertThat(empty2).isInstanceOf(EmptyChannelzScheduledReporter.class);
    }

    @Test
    public void shouldReturnDefaultWhenDurationAboveZero() {
        ChannelzScheduledReporter defaultReporter = new ChannelzScheduledReporterBuilder()
                .setReportPeriod(Duration.ofMillis(1))
                .build();
        assertThat(defaultReporter).isInstanceOf(DefaultChannelzScheduledReporter.class);
    }

}
