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

import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.vo.AgentInfoFilter;
import com.navercorp.pinpoint.web.vo.ApplicationAgentsList;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Taejin Koo
 */
public class AgentCountReader implements ItemReader<ApplicationAgentsList>, StepExecutionListener {

    @Autowired
    private AgentInfoService agentInfoService;

    private final Queue<ApplicationAgentsList> queue = new LinkedList<>();

    @Override
    public void beforeStep(StepExecution stepExecution) {
        long timestamp = System.currentTimeMillis();
        ApplicationAgentsList applicationAgentList = agentInfoService.getAllApplicationAgentsList(AgentInfoFilter::accept, timestamp);
        queue.add(applicationAgentList);
    }

    @Override
    public ApplicationAgentsList read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return queue.poll();
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }

}
