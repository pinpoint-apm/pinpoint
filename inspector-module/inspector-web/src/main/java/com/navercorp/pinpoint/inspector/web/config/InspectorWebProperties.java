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

package com.navercorp.pinpoint.inspector.web.config;

import com.navercorp.pinpoint.common.server.config.AnnotationVisitor;
import com.navercorp.pinpoint.common.server.config.LoggingEvent;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author minwoo-jung
 */
public class InspectorWebProperties {

    private final Logger logger = LogManager.getLogger(InspectorWebProperties.class);

    @Value("${pinot.inspector.agent.table.count}")
    private int agentStatTableCount;
    @Value("${pinot.inspector.agent.table.prefix}")
    private String agentStatTablePrefix;
    @Value("${pinot.inspector.agent.table.padding.length}")
    private int agentStatTablePaddingLength;
    @Value("${web.inspector.api.period.max:14}")
    private int inspectorPeriodMax;

    @PostConstruct
    public void log() {
        logger.info("{}", this);
        AnnotationVisitor<Value> annotationVisitor = new AnnotationVisitor<>(Value.class);
        annotationVisitor.visit(this, new LoggingEvent(this.logger));
    }


    public int getAgentStatTableCount() {
        return agentStatTableCount;
    }

    public String getAgentStatTablePrefix() {
        return agentStatTablePrefix;
    }

    public int getAgentStatTablePaddingLength() {
        return agentStatTablePaddingLength;
    }

    public int getInspectorPeriodMax() {
        return inspectorPeriodMax;
    }

    @Override
    public String toString() {
        return "InspectorWebProperties{" +
                "agentStatTableCount=" + agentStatTableCount +
                ", agentStatTablePrefix='" + agentStatTablePrefix + '\'' +
                ", agentStatTablePaddingLength=" + agentStatTablePaddingLength +
                ", inspectorPeriodMax=" + inspectorPeriodMax +
                '}';
    }
}
