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
package com.navercorp.pinpoint.flink.receiver;

import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.collector.receiver.AbstractDispatchHandler;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStatBatch;
import org.apache.thrift.TBase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class TcpDispatchHandler extends AbstractDispatchHandler {

    private  AgentStatHandler agentStatHandler;

    @Override
    protected List<SimpleHandler> getSimpleHandler(TBase<?, ?> tBase) {
        List<SimpleHandler> handlerList = new ArrayList<>();

        if (tBase instanceof TFAgentStatBatch) {
            handlerList.add(agentStatHandler);
        }

        return handlerList;
    }

    public void setAgentStatHandler(AgentStatHandler agentStatHandler) {
        this.agentStatHandler = agentStatHandler;
    }

}
