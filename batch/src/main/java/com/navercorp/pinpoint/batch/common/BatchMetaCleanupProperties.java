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

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Settings of {@code batchMetaCleanupJob} ({@code job.cleanup.batch-meta.*}): the job that deletes the
 * Spring Batch meta data ({@code BATCH_*} tables) of job instances older than a retention period.
 * <p>
 * Every pinpoint job runs with {@code schedule.date} as an identifying parameter, so each run leaves a new
 * job instance behind and nothing is ever restarted; the meta data has no restart value, only an audit one.
 * The retention per job overrides the default; {@code otlpLinkJoinJob} needs at least one hour, and at least
 * {@code job.otlp.link-join.ttl} ({@link #requireRetention}), because its launcher and cleanup step read the
 * recent executions.
 */
public class BatchMetaCleanupProperties {

    public static final String PREFIX = "job.cleanup.batch-meta";

    /** the launcher and the cleanup guard of otlpLinkJoinJob read the executions of the last few minutes */
    static final Duration MIN_RETENTION = Duration.ofHours(1);
    static final int MIN_PAGE_SIZE = 100;
    static final int MAX_PAGE_SIZE = 10_000;

    private boolean enable = false;
    private String cron = "0 30 4 * * *";
    private boolean dryRun = true;
    private Duration retention = Duration.ofDays(30);
    private Map<String, Duration> retentionByJob = new LinkedHashMap<>();
    private List<String> jobNames = new ArrayList<>();
    private int maxInstancesPerRun = 50_000;
    private int pageSize = 1_000;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public Duration getRetention() {
        return retention;
    }

    public void setRetention(Duration retention) {
        this.retention = retention;
    }

    public Map<String, Duration> getRetentionByJob() {
        return retentionByJob;
    }

    public void setRetentionByJob(Map<String, Duration> retentionByJob) {
        this.retentionByJob = retentionByJob == null ? new LinkedHashMap<>() : new LinkedHashMap<>(retentionByJob);
    }

    /**
     * Job names whose meta data the job may delete. Empty means the jobs registered in this application;
     * the jobs of other applications sharing the meta tables are then left alone.
     */
    public List<String> getJobNames() {
        return jobNames;
    }

    public void setJobNames(List<String> jobNames) {
        this.jobNames = jobNames == null ? new ArrayList<>() : new ArrayList<>(jobNames);
    }

    public int getMaxInstancesPerRun() {
        return maxInstancesPerRun;
    }

    public void setMaxInstancesPerRun(int maxInstancesPerRun) {
        this.maxInstancesPerRun = maxInstancesPerRun;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Retention of {@code jobName}: its own entry when there is one (job names are matched ignoring case,
     * since property map keys may be written in lower case), otherwise the default.
     */
    public Duration retentionOf(String jobName) {
        Objects.requireNonNull(jobName, "jobName");
        for (Map.Entry<String, Duration> entry : retentionByJob.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(jobName) && entry.getValue() != null) {
                return entry.getValue();
            }
        }
        return retention;
    }

    /** the shortest retention in effect: executions younger than this are kept whatever the job */
    public Duration minRetention() {
        Duration min = retention;
        for (Duration value : retentionByJob.values()) {
            if (value != null && value.compareTo(min) < 0) {
                min = value;
            }
        }
        return min;
    }

    /**
     * Whether the meta data of {@code jobName} may be deleted: listed in {@code job-names}, or, when the list
     * is empty, registered in this application.
     */
    public boolean covers(String jobName, boolean registeredHere) {
        Objects.requireNonNull(jobName, "jobName");
        if (jobNames.isEmpty()) {
            return registeredHere;
        }
        for (String name : jobNames) {
            if (name.equalsIgnoreCase(jobName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fails when the effective retention of {@code jobName} is shorter than {@code minimum}: for a job that
     * reads its own recent meta data (otlpLinkJoinJob revisits the slots of the last {@code job.otlp.link-join.ttl}
     * through their executions), deleting earlier would run those slots again.
     */
    public void requireRetention(String jobName, Duration minimum, String reason) {
        Objects.requireNonNull(minimum, "minimum");
        final Duration effective = retentionOf(jobName);
        if (effective.compareTo(minimum) < 0) {
            throw new IllegalArgumentException(PREFIX + ".retention-by-job." + jobName + " (effective " + effective
                    + ") must be at least " + minimum + ": " + reason);
        }
    }

    public void validate() {
        if (retention == null || retention.compareTo(MIN_RETENTION) < 0) {
            throw new IllegalArgumentException(PREFIX + ".retention must be at least " + MIN_RETENTION + ": " + retention);
        }
        for (Map.Entry<String, Duration> entry : retentionByJob.entrySet()) {
            if (entry.getValue() == null || entry.getValue().compareTo(MIN_RETENTION) < 0) {
                throw new IllegalArgumentException(PREFIX + ".retention-by-job." + entry.getKey() + " must be at least " + MIN_RETENTION + ": " + entry.getValue());
            }
        }
        if (maxInstancesPerRun < 1) {
            throw new IllegalArgumentException(PREFIX + ".max-instances-per-run must be positive: " + maxInstancesPerRun);
        }
        if (pageSize < MIN_PAGE_SIZE || pageSize > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(PREFIX + ".page-size must be between " + MIN_PAGE_SIZE + " and " + MAX_PAGE_SIZE + ": " + pageSize);
        }
        if (cron == null || cron.isBlank()) {
            throw new IllegalArgumentException(PREFIX + ".cron must not be empty");
        }
    }

    @Override
    public String toString() {
        return "BatchMetaCleanupProperties{" +
                "enable=" + enable +
                ", cron='" + cron + '\'' +
                ", dryRun=" + dryRun +
                ", retention=" + retention +
                ", retentionByJob=" + retentionByJob +
                ", jobNames=" + jobNames +
                ", maxInstancesPerRun=" + maxInstancesPerRun +
                ", pageSize=" + pageSize +
                '}';
    }
}
