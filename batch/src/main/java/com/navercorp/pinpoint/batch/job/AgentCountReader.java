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

import com.navercorp.pinpoint.batch.service.BatchApplicationIndexService;
import jakarta.annotation.Nonnull;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author youngjin.kim2
 */
public class AgentCountReader implements ItemReader<String>, StepExecutionListener {

    private final BatchApplicationIndexService batchApplicationIndexService;

    private Queue<String> applicationNameQueue;

    public AgentCountReader(BatchApplicationIndexService batchApplicationIndexService) {
        this.batchApplicationIndexService = batchApplicationIndexService;
    }

    @Override
    public void beforeStep(@Nonnull StepExecution stepExecution) {
        List<String> applicationNames = batchApplicationIndexService.selectAllApplicationNames();
        this.applicationNameQueue = new ConcurrentLinkedQueue<>(applicationNames);
    }

    @Override
    public ExitStatus afterStep(@Nonnull StepExecution stepExecution) {
        return null;
    }

    @Override
    public String read() {
        return applicationNameQueue.poll();
    }

}
