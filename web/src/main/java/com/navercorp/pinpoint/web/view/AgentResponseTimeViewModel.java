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
import java.util.Objects;

/**
 * @author emeroad
 */
@JsonSerialize(using=AgentResponseTimeViewModelSerializer.class)
public class AgentResponseTimeViewModel {

    private final Application agentName;

    private final List<TimeViewModel> responseTimeViewModel;

    public AgentResponseTimeViewModel(Application agentName, List<TimeViewModel> responseTimeViewModel) {
        this.agentName = Objects.requireNonNull(agentName, "agentName");
        this.responseTimeViewModel = Objects.requireNonNull(responseTimeViewModel, "responseTimeViewModel");
    }

    public String getAgentName() {
        return agentName.getName();
    }

    public List<TimeViewModel> getResponseTimeViewModel() {
        return responseTimeViewModel;
    }
}
