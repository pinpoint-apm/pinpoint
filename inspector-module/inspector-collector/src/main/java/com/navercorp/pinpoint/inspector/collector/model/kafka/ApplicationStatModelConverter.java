/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.inspector.collector.model.kafka;

import com.navercorp.pinpoint.metric.common.model.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo-jung
 */
public class ApplicationStatModelConverter {

    private final static Logger logger = LogManager.getLogger(ApplicationStatModelConverter.class.getName());

    public static List<ApplicationStat> convertToApplicationStat(List<AgentStat> agentStatList) {
        List<ApplicationStat> applicationStatList = new ArrayList<>(agentStatList.size());

        for (AgentStat agentStat : agentStatList) {
            applicationStatList.add(new ApplicationStat(agentStat.getTenantId(),
                                                        agentStat.getApplicationName(),
                                                        agentStat.getMetricName(),
                                                        agentStat.getFieldName(),
                                                        agentStat.getFieldValue(),
                                                        agentStat.getEventTime()));
        }

        return applicationStatList;
    }

    public static List<ApplicationStat> convertFromDataSourceStatToApplicationStat(List<AgentStat> agentStatList) {
        List<ApplicationStat> applicationStatList = new ArrayList<>(agentStatList.size());

        for (AgentStat agentStat : agentStatList) {
            List<Tag> tags= agentStat.getTags();
            Tag jdbcUrlTag = null;
            for (Tag tag : tags) {
                if (AgentStatModelConverter.DATASOUCE_TAG_JDBC_URL_KEY.equals(tag.getName())) {
                    jdbcUrlTag = tag;
                    break;
                };
            }

            if (jdbcUrlTag == null) {
                continue;
            }

            applicationStatList.add(new ApplicationStat(agentStat.getTenantId(),
                                                        agentStat.getApplicationName(),
                                                        agentStat.getMetricName(),
                                                        agentStat.getFieldName(),
                                                        jdbcUrlTag.toString(),
                                                        agentStat.getFieldValue(),
                                                        agentStat.getEventTime()));
        }

        return applicationStatList;
    }
}
