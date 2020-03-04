/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.rpc.util.ClassUtils;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCountRes;
import com.navercorp.pinpoint.web.websocket.ActiveThreadCountErrorType;
import org.apache.thrift.TBase;

import java.util.Collections;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class AgentActiveThreadCountFactory {

    static final ActiveThreadCountErrorType INTERNAL_ERROR = ActiveThreadCountErrorType.PINPOINT_INTERNAL_ERROR;

    private String agentId;

    public AgentActiveThreadCountFactory() {
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public AgentActiveThreadCount create(TBase<?, ?> value) {
        if (agentId == null) {
            throw new NullPointerException("agentId");
        }

        if (value instanceof TCmdActiveThreadCountRes) {
            TCmdActiveThreadCountRes response = (TCmdActiveThreadCountRes) value;
            List<Integer> activeThreadCountList = response.getActiveThreadCount();
            if (CollectionUtils.nullSafeSize(activeThreadCountList) == 4) {
                return createSuccess0(activeThreadCountList);
            } else {
                return createFail(INTERNAL_ERROR.getCode(), "activeThreadCountList size must be 4");
            }
        } else {
            StringBuilder message = new StringBuilder();
            message.append("agentId:").append(agentId);
            message.append("- value(").append(ClassUtils.simpleClassName(value));
            message.append(") must be an instance of TCmdActiveThreadCountRes");

            return createFail0(INTERNAL_ERROR.getCode(), message.toString());
        }
    }

    public AgentActiveThreadCount createFail(String message) {
        return createFail(INTERNAL_ERROR.getCode(), message);
    }

    public AgentActiveThreadCount createFail(short code, String message) {
        if (agentId == null) {
            throw new NullPointerException("agentId");
        }

        return createFail0(code, message);
    }


    private AgentActiveThreadCount createSuccess0(List<Integer> activeThreadCountList) {
        if (CollectionUtils.nullSafeSize(activeThreadCountList) != 4) {
            throw new IllegalArgumentException("activeThreadCountList size must be 4");
        }

        AgentActiveThreadCount.Builder builder = new AgentActiveThreadCount.Builder();
        builder.setAgentId(agentId);
        builder.setActiveThreadCountList(activeThreadCountList);
        builder.setStatus(AgentActiveThreadCount.Builder.SUCCESS_STATUS);
        return builder.build();
    }

    private AgentActiveThreadCount createFail0(short code, String codeMessage) {
        AgentActiveThreadCount.Builder builder = new AgentActiveThreadCount.Builder();
        builder.setAgentId(agentId);
        builder.setActiveThreadCountList(Collections.emptyList());
        builder.setStatus(code, codeMessage);
        return builder.build();
    }

}
