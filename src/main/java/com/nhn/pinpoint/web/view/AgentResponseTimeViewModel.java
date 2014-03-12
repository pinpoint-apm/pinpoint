package com.nhn.pinpoint.web.view;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

/**
 * @author emeroad
 */
@JsonSerialize(using=AgentResponseTimeViewModelSerializer.class)
public class AgentResponseTimeViewModel {

    private final String agentName;

    private final List<ResponseTimeViewModel> responseTimeViewModel;

    public AgentResponseTimeViewModel(String agentName, List<ResponseTimeViewModel> responseTimeViewModel) {
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
        return agentName;
    }

    public List<ResponseTimeViewModel> getResponseTimeViewModel() {
        return responseTimeViewModel;
    }

}
