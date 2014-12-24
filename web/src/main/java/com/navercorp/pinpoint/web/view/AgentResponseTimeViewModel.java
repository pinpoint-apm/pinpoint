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
import com.navercorp.pinpoint.web.vo.Application;

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
