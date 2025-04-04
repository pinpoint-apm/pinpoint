/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.inspector.collector.dao.pinot;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.StatDataPoint;
import com.navercorp.pinpoint.inspector.collector.model.kafka.AgentStat;
import com.navercorp.pinpoint.inspector.collector.model.kafka.ApplicationStat;
import com.navercorp.pinpoint.inspector.collector.model.kafka.ApplicationStatModelConverter;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author minwoo.jung
 */
public class PinotTypeMapper<T extends StatDataPoint> {

    private static final ApplicationStatModelConverter APP_MAPPER = new ApplicationStatModelConverter();

    private final Function<AgentStatBo, List<T>> point;
    private final BiFunction<List<T>, String, List<AgentStat>> agentStat;
    private final Function<List<AgentStat>, List<ApplicationStat>> applicationStat;

    public PinotTypeMapper(Function<AgentStatBo, List<T>> point,
                           BiFunction<List<T>, String, List<AgentStat>> agentStat,
                           Function<List<AgentStat>, List<ApplicationStat>> applicationStat) {
        this.point = Objects.requireNonNull(point, "point");
        this.agentStat = Objects.requireNonNull(agentStat, "agentStat");
        this.applicationStat = Objects.requireNonNull(applicationStat, "applicationStat");
    }

    public PinotTypeMapper(Function<AgentStatBo, List<T>> point,
                           BiFunction<List<T>, String, List<AgentStat>> agentStat) {
        this(point, agentStat, APP_MAPPER::convertToApplicationStat);
    }

    public List<T> point(AgentStatBo agentStatBo) {
        return point.apply(agentStatBo);
    }

    public List<AgentStat> agentStat(List<T> points, String tenantId) {
        return agentStat.apply(points, tenantId);
    }

    public List<ApplicationStat> applicationStat(List<AgentStat> agentStats) {
        return applicationStat.apply(agentStats);
    }
}
