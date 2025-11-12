/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.config;

import com.navercorp.pinpoint.web.applicationmap.nodes.AgentServerGroupListWriter;
import com.navercorp.pinpoint.web.applicationmap.service.AlertViewService;
import com.navercorp.pinpoint.web.applicationmap.view.LinkView;
import com.navercorp.pinpoint.web.applicationmap.view.NodeView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapViewConfiguration {

    @Bean
    public AlertViewService alertViewService() {
        return new AlertViewService();
    }

    @Bean
    public AgentServerGroupListWriter agentServerGroupListWriter() {
        return new AgentServerGroupListWriter();
    }

    @Bean
    public NodeView.NodeViewSerializer nodeViewSerializer(AlertViewService alertViewService, AgentServerGroupListWriter agentServerGroupListWriter) {
        return new NodeView.NodeViewSerializer(alertViewService, agentServerGroupListWriter);
    }

    @Bean
    public LinkView.LinkViewSerializer linkViewSerializer(AlertViewService alertViewService, AgentServerGroupListWriter agentServerGroupListWriter) {
        return new LinkView.LinkViewSerializer(alertViewService, agentServerGroupListWriter);
    }
}
