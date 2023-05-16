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

package com.navercorp.pinpoint.common.server.bo.stat.join;

import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Taejin Koo
 */
public class JoinDoubleFieldBo extends AbstractJoinFieldBo<Double> {

    private static final double UNCOLLECTED_VALUE = -1;
    static final JoinDoubleFieldBo UNCOLLECTED_FIELD_BO = new JoinDoubleFieldBo(UNCOLLECTED_VALUE, UNCOLLECTED_VALUE, JoinStatBo.UNKNOWN_AGENT, UNCOLLECTED_VALUE, JoinStatBo.UNKNOWN_AGENT);

    public JoinDoubleFieldBo(Double avgValue, Double minValue, String minAgentId, Double maxValue, String maxAgentId) {
        super(avgValue, minValue, minAgentId, maxValue, maxAgentId);
    }

    @Override
    protected JoinFieldBo<Double> getUncollectedValue() {
        return UNCOLLECTED_FIELD_BO;
    }

    public JoinLongFieldBo toLongFieldBo() {
        final long avg = AgentStatUtils.convertDoubleToLong(getAvg());
        final long min = AgentStatUtils.convertDoubleToLong(getMin());
        final long max = AgentStatUtils.convertDoubleToLong(getMax());

        return new JoinLongFieldBo(avg, min, getMinAgentId(), max, getMaxAgentId());
    }

    protected static JoinDoubleFieldBo merge(List<JoinDoubleFieldBo> joinDoubleFieldBoList) {
        if (CollectionUtils.isEmpty(joinDoubleFieldBoList)) {
            return UNCOLLECTED_FIELD_BO;
        }

        double avg = joinDoubleFieldBoList.stream()
                .mapToDouble(JoinFieldBo::getAvg)
                .average()
                .orElseThrow(NoSuchElementException::new);
        JoinDoubleFieldBo max = joinDoubleFieldBoList.stream()
                .max(Comparator.comparing(JoinFieldBo::getMax))
                .orElseThrow(NoSuchElementException::new);
        JoinDoubleFieldBo min = joinDoubleFieldBoList.stream()
                .min(Comparator.comparing(JoinFieldBo::getMin))
                .orElseThrow(NoSuchElementException::new);

        String maxAgentId = max.getMaxAgentId();
        double maxValue = max.getMax();

        String minAgentId = min.getMinAgentId();
        double minValue = min.getMin();

        return new JoinDoubleFieldBo(avg, minValue, minAgentId, maxValue, maxAgentId);
    }

}

