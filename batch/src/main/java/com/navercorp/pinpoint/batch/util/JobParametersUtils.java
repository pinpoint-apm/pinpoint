/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.batch.util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class JobParametersUtils {

    public static final String SCHEDULE_DATE_KEY = "schedule.date";

    private JobParametersUtils() {
    }

    public static JobParametersBuilder newJobParametersBuilder() {
        JobParametersBuilder builder = new JobParametersBuilder();
        Date now = new Date();
        builder.addDate(SCHEDULE_DATE_KEY, now);
        return builder;
    }

    public static @Nullable Date getScheduleDate(@NonNull ChunkContext chunkContext) {
        StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();
        return stepExecution.getJobExecution().getJobParameters().getDate(SCHEDULE_DATE_KEY);
    }

    public static long getScheduleTime(JobExecution execution) {
        Date scheduleDate = execution.getJobParameters().getDate(SCHEDULE_DATE_KEY);
        if (scheduleDate != null) {
            return scheduleDate.getTime();
        }
        return getCreateTime(execution);
    }

    public static long getCreateTime(JobExecution execution) {
        LocalDateTime createTime = execution.getCreateTime();
        return createTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
