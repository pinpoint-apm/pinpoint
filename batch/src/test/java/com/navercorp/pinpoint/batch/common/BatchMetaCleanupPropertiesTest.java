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

package com.navercorp.pinpoint.batch.common;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BatchMetaCleanupPropertiesTest {

    private static BatchMetaCleanupProperties bind(Map<String, String> properties) {
        Binder binder = new Binder(new MapConfigurationPropertySource(properties));
        return binder.bind(BatchMetaCleanupProperties.PREFIX, Bindable.of(BatchMetaCleanupProperties.class))
                .orElseGet(BatchMetaCleanupProperties::new);
    }

    @Test
    void defaultsAreOffDryRunAndThirtyDays() {
        BatchMetaCleanupProperties properties = bind(Map.of());
        properties.validate();
        assertThat(properties.isEnable()).isFalse();
        assertThat(properties.isDryRun()).isTrue();
        assertThat(properties.getRetention()).isEqualTo(Duration.ofDays(30));
        assertThat(properties.getCron()).isEqualTo("0 30 4 * * *");
        assertThat(properties.getJobNames()).isEmpty();
        assertThat(properties.getMaxInstancesPerRun()).isEqualTo(50_000);
        assertThat(properties.getPageSize()).isEqualTo(1_000);
        assertThat(properties.retentionOf("alarmJob")).isEqualTo(Duration.ofDays(30));
        assertThat(properties.minRetention()).isEqualTo(Duration.ofDays(30));
    }

    @Test
    void bindsDurationsAndTheRetentionPerJob() {
        BatchMetaCleanupProperties properties = bind(Map.of(
                "job.cleanup.batch-meta.enable", "true",
                "job.cleanup.batch-meta.dry-run", "false",
                "job.cleanup.batch-meta.retention", "14d",
                "job.cleanup.batch-meta.retention-by-job.otlplinkjoinjob", "1d",
                "job.cleanup.batch-meta.retention-by-job.alarmJob", "7d",
                "job.cleanup.batch-meta.job-names", "alarmJob, agentCountJob",
                "job.cleanup.batch-meta.max-instances-per-run", "100000",
                "job.cleanup.batch-meta.page-size", "500"));
        properties.validate();

        assertThat(properties.isEnable()).isTrue();
        assertThat(properties.isDryRun()).isFalse();
        assertThat(properties.getRetention()).isEqualTo(Duration.ofDays(14));
        // map keys may lose their case on the way in: matched ignoring case
        assertThat(properties.retentionOf("otlpLinkJoinJob")).isEqualTo(Duration.ofDays(1));
        assertThat(properties.retentionOf("alarmJob")).isEqualTo(Duration.ofDays(7));
        assertThat(properties.retentionOf("agentCountJob")).isEqualTo(Duration.ofDays(14));
        assertThat(properties.minRetention()).isEqualTo(Duration.ofDays(1));
        assertThat(properties.getJobNames()).containsExactly("alarmJob", "agentCountJob");
        assertThat(properties.getMaxInstancesPerRun()).isEqualTo(100_000);
        assertThat(properties.getPageSize()).isEqualTo(500);
    }

    @Test
    void retentionBelowOneHourIsRejected() {
        BatchMetaCleanupProperties properties = new BatchMetaCleanupProperties();
        properties.setRetention(Duration.ofMinutes(30));
        assertThatThrownBy(properties::validate).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("retention");

        BatchMetaCleanupProperties perJob = new BatchMetaCleanupProperties();
        perJob.setRetentionByJob(Map.of("otlpLinkJoinJob", Duration.ofMinutes(5)));
        assertThatThrownBy(perJob::validate).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("otlpLinkJoinJob");
    }

    @Test
    void theRetentionOfAJobThatReadsItsOwnMetaDataMustCoverItsTtl() {
        BatchMetaCleanupProperties properties = new BatchMetaCleanupProperties();
        properties.setRetentionByJob(Map.of("otlpLinkJoinJob", Duration.ofHours(1)));
        properties.validate();

        // ttl 5m (default) under a retention of 1h: fine
        properties.requireRetention("otlpLinkJoinJob", Duration.ofMinutes(5), "ttl");
        // ttl 2h over a retention of 1h: the one-hour minimum alone would let the slots of the last 2h be deleted
        assertThatThrownBy(() -> properties.requireRetention("otlpLinkJoinJob", Duration.ofHours(2), "ttl is 2h"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("otlpLinkJoinJob")
                .hasMessageContaining("PT1H")
                .hasMessageContaining("PT2H")
                .hasMessageContaining("ttl is 2h");
        // the default retention applies to a job without its own entry
        properties.requireRetention("alarmJob", Duration.ofDays(30), "default");
        assertThatThrownBy(() -> properties.requireRetention("alarmJob", Duration.ofDays(31), "default"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void coversTheListedJobsOrTheRegisteredOnesWhenNothingIsListed() {
        BatchMetaCleanupProperties properties = new BatchMetaCleanupProperties();
        assertThat(properties.covers("otlpLinkJoinJob", true)).isTrue();
        assertThat(properties.covers("otlpLinkJoinJob", false)).isFalse();

        properties.setJobNames(java.util.List.of("alarmJob", "otlplinkjoinjob"));
        assertThat(properties.covers("otlpLinkJoinJob", false)).isTrue();
        assertThat(properties.covers("agentCountJob", true)).isFalse();
    }

    @Test
    void capAndPageSizeAreBounded() {
        BatchMetaCleanupProperties cap = new BatchMetaCleanupProperties();
        cap.setMaxInstancesPerRun(0);
        assertThatThrownBy(cap::validate).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("max-instances-per-run");

        BatchMetaCleanupProperties page = new BatchMetaCleanupProperties();
        page.setPageSize(50);
        assertThatThrownBy(page::validate).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("page-size");
    }
}
