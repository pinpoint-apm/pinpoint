/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.metric.CustomMetricType;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatHbaseOperationFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Taejin Koo
 */
@Component
public class AgentCustomMetricServiceRegistry implements AgentCustomMetricServiceLocator {

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Autowired
    private TableNameProvider tableNameProvider;

    @Autowired
    private AgentStatHbaseOperationFactory agentStatHbaseOperationFactory;

    private List<AgentCustomMetricService> agentCustomMetricServiceList = new ArrayList<>();

    @PostConstruct
    public void setup() {
        final HbaseAgentCustomMetricService hbaseAgentCustomMetricService = new HbaseAgentCustomMetricService(hbaseTemplate, tableNameProvider, agentStatHbaseOperationFactory, CustomMetricType.NETTY_DIRECT_BUFFER);

        agentCustomMetricServiceList.add(hbaseAgentCustomMetricService);
    }

    @Override
    public List<AgentCustomMetricService> getAgentCustomMetricService() {
        return Collections.unmodifiableList(agentCustomMetricServiceList);
    }

}
