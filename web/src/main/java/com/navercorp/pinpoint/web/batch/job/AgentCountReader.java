package com.navercorp.pinpoint.web.batch.job;

import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.vo.ApplicationAgentList;
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
public class AgentCountReader implements ItemReader<ApplicationAgentList>, StepExecutionListener {

    @Autowired
    private AgentInfoService agentInfoService;

    private final Queue<ApplicationAgentList> queue = new LinkedList<>();

    @Override
    public void beforeStep(StepExecution stepExecution) {
        ApplicationAgentList applicationAgentList = agentInfoService.getApplicationAgentList(ApplicationAgentList.Key.APPLICATION_NAME);
        queue.add(applicationAgentList);
    }

    @Override
    public ApplicationAgentList read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return queue.poll();
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }

}
