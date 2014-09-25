package com.nhn.pinpoint.web.view;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

/**
 * @author emeroad
 */
@JsonSerialize(using = AgentResponseTimeViewModelListSerializer.class)
public class AgentResponseTimeViewModelList {

    public static final String DEFAULT_FIELD_NAME = "agentTimeSeriesHistogram";

    private String fieldName;
    private final List<AgentResponseTimeViewModel> agentResponseTimeViewModelList;

    public AgentResponseTimeViewModelList(List<AgentResponseTimeViewModel> agentResponseTimeViewModelList) {
        this(DEFAULT_FIELD_NAME, agentResponseTimeViewModelList);
    }

    public AgentResponseTimeViewModelList(String fieldName, List<AgentResponseTimeViewModel> agentResponseTimeViewModelList) {
        if (fieldName == null) {
            throw new NullPointerException("fieldName must not be null");
        }
        if (agentResponseTimeViewModelList == null) {
            throw new NullPointerException("agentResponseTimeViewModelList must not be null");
        }
        this.fieldName = fieldName;
        this.agentResponseTimeViewModelList = agentResponseTimeViewModelList;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public List<AgentResponseTimeViewModel> getAgentResponseTimeViewModelList() {
        return agentResponseTimeViewModelList;
    }
}
