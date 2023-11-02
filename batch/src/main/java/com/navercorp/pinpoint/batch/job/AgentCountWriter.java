/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.batch.job;

import com.navercorp.pinpoint.web.dao.AgentStatisticsDao;
import com.navercorp.pinpoint.web.util.DateTimeUtils;
import com.navercorp.pinpoint.web.vo.AgentCountStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.*;
import org.springframework.batch.item.ItemWriter;

import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author youngjin.kim2
 */
public class AgentCountWriter implements ItemWriter<Integer>, StepExecutionListener {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final AgentStatisticsDao agentStatisticsDao;
    private final AtomicInteger count = new AtomicInteger(0);
    private final long timestamp = DateTimeUtils.timestampToStartOfDay(System.currentTimeMillis());

    public AgentCountWriter(AgentStatisticsDao agentStatisticsDao) {
        this.agentStatisticsDao = Objects.requireNonNull(agentStatisticsDao, "agentStatisticsDao");
    }

    @Override
    public void beforeStep(@Nonnull StepExecution stepExecution) {}

    @Override
    public ExitStatus afterStep(@Nonnull StepExecution stepExecution) {
        if (!stepExecution.getFailureExceptions().isEmpty()) {
            Throwable t = stepExecution.getFailureExceptions().get(0);
            logger.error("Error detected while counting active agents", t);
            return ExitStatus.FAILED;
        }

        if (stepExecution.getStatus().equals(BatchStatus.FAILED)) {
            logger.error("Failed to count active agents");
            return ExitStatus.FAILED;
        }

        try {
            writeCount(count.get());
        } catch (JobExecutionException e) {
            logger.error("Failed to store agentCount in DB (agentCount: {})", count.get(), e);
            return ExitStatus.FAILED;
        }
        return ExitStatus.COMPLETED;
    }

    @Override
    public void write(List<? extends Integer> items) {
        count.getAndAdd(items.stream().mapToInt(el -> el).sum());
    }

    private void writeCount(int count) throws JobExecutionException {
        logger.info("{} agents are alive", count);

        AgentCountStatistics statistics = new AgentCountStatistics(count, timestamp);

        if (!agentStatisticsDao.insertAgentCount(statistics)) {
            throw new JobExecutionException("Failed to insert AgentCountStatistics: " + statistics);
        }
    }

}
