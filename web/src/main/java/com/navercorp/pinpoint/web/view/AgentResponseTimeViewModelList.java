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

package com.navercorp.pinpoint.web.view;

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
