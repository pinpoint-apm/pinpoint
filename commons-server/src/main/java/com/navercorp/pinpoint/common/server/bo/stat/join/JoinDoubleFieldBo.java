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

import java.util.List;

/**
 * @author Taejin Koo
 */
public class JoinDoubleFieldBo extends AbstractJoinFieldBo<Double> {

    private static final double UNCOLLECTED_VALUE = -1;
    static final JoinDoubleFieldBo UNCOLLECTED_FIELD_BO = new JoinDoubleFieldBo(UNCOLLECTED_VALUE, UNCOLLECTED_VALUE, JoinStatBo.UNKNOWN_AGENT, UNCOLLECTED_VALUE, JoinStatBo.UNKNOWN_AGENT);

    public JoinDoubleFieldBo(Double value, Double minValue, String minAgentId, Double maxValue, String maxAgentid) {
        super(value, minValue, minAgentId, maxValue, maxAgentid);
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
        int size = CollectionUtils.nullSafeSize(joinDoubleFieldBoList);
        if (size == 0) {
            return UNCOLLECTED_FIELD_BO;
        }

        JoinFieldBo<Double> firstJoinDoubleFieldBo = joinDoubleFieldBoList.get(0);
        double sumTotalValue = 0;

        String maxAgentId = firstJoinDoubleFieldBo.getMaxAgentId();
        double maxValue = firstJoinDoubleFieldBo.getMax();

        String minAgentId = firstJoinDoubleFieldBo.getMinAgentId();
        double minValue = firstJoinDoubleFieldBo.getMin();

        for (JoinFieldBo<Double> joinDoubleFieldBo : joinDoubleFieldBoList) {
            sumTotalValue += joinDoubleFieldBo.getAvg();

            if (joinDoubleFieldBo.getMax() > maxValue) {
                maxValue = joinDoubleFieldBo.getMax();
                maxAgentId = joinDoubleFieldBo.getMaxAgentId();
            }

            if (joinDoubleFieldBo.getMin() < minValue) {
                minValue = joinDoubleFieldBo.getMin();
                minAgentId = joinDoubleFieldBo.getMinAgentId();
            }
        }

        return new JoinDoubleFieldBo(sumTotalValue / size, minValue, minAgentId, maxValue, maxAgentId);
    }

}

