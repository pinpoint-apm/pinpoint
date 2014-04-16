package com.nhn.pinpoint.web.view;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.nhn.pinpoint.web.vo.Application;

import java.util.List;

/**
 * @author emeroad
 */
@JsonSerialize(using=AgentResponseTimeViewModelSerializer.class)
public class AgentResponseTimeViewModel {

    private final Application agentName;

    private final List<ResponseTimeViewModel> responseTimeViewModel;

    public AgentResponseTimeViewModel(Application agentName, List<ResponseTimeViewModel> responseTimeViewModel) {
        if (agentName == null) {
            throw new NullPointerException("agentName must not be null");
        }
        if (responseTimeViewModel == null) {
            throw new NullPointerException("responseTimeViewModel must not be null");
        }
        this.agentName = agentName;
        this.responseTimeViewModel = responseTimeViewModel;
    }

    public String getAgentName() {
        return agentName.getName();
    }

    public List<ResponseTimeViewModel> getResponseTimeViewModel() {
        return responseTimeViewModel;
    }

}
