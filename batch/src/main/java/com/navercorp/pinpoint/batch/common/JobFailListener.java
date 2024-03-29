/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.batch.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import java.util.Optional;

/**
 * @author minwoo.jung<minwoo.jung@navercorp.com>
 */
public class JobFailListener implements JobExecutionListener {

    private static final Logger logger = LogManager.getLogger(JobFailListener.class);

    private final JobFailMessageSender jobFailMessageSender;

    public JobFailListener(Optional<JobFailMessageSender> jobFailMessageSender) {
        this.jobFailMessageSender = jobFailMessageSender.orElseGet(EmptyJobFailMessageSender::new);
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (!jobExecution.getExitStatus().equals(ExitStatus.COMPLETED)) {
            for (Throwable throwable : jobExecution.getAllFailureExceptions()) {
                logger.error("job fail. exception message : " , throwable);
            }

            jobFailMessageSender.sendSMS(jobExecution);
            jobFailMessageSender.sendEmail(jobExecution);
        }
    }

}
