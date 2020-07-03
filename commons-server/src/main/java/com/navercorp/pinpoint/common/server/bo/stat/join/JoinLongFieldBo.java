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
public class JoinLongFieldBo extends AbstractJoinFieldBo<Long> {

    private static final long UNCOLLECTED_VALUE = -1L;
    static final JoinLongFieldBo UNCOLLECTED_FIELD_BO = new JoinLongFieldBo(UNCOLLECTED_VALUE, UNCOLLECTED_VALUE, JoinStatBo.UNKNOWN_AGENT, UNCOLLECTED_VALUE, JoinStatBo.UNKNOWN_AGENT);

    public JoinLongFieldBo(Long value, Long minValue, String minAgentId, Long maxValue, String maxAgentid) {
        super(value, minValue, minAgentId, maxValue, maxAgentid);
    }

    @Override
    protected JoinFieldBo<Long> getUncollectedValue() {
        return UNCOLLECTED_FIELD_BO;
    }

    public JoinDoubleFieldBo toLongFieldBo() {
        double avg = AgentStatUtils.convertLongToDouble(getAvg());
        double min = AgentStatUtils.convertLongToDouble(getMin());
        double max = AgentStatUtils.convertLongToDouble(getMax());
        return new JoinDoubleFieldBo(avg, min, getMinAgentId(), max, getMaxAgentId());
    }

    protected static JoinLongFieldBo merge(List<JoinLongFieldBo> joinLongFieldBoList) {
        int size = CollectionUtils.nullSafeSize(joinLongFieldBoList);
        if (size == 0) {
            return UNCOLLECTED_FIELD_BO;
        }

        JoinFieldBo<Long> firstJoinLongFieldBo = joinLongFieldBoList.get(0);
        long sumTotalValue = 0;

        String maxAgentId = firstJoinLongFieldBo.getMaxAgentId();
        long maxValue = firstJoinLongFieldBo.getMax();

        String minAgentId = firstJoinLongFieldBo.getMinAgentId();
        long minValue = firstJoinLongFieldBo.getMin();

        for (JoinFieldBo<Long> joinLongFieldBo : joinLongFieldBoList) {
            sumTotalValue += joinLongFieldBo.getAvg();

            if (joinLongFieldBo.getMax() > maxValue) {
                maxValue = joinLongFieldBo.getMax();
                maxAgentId = joinLongFieldBo.getMaxAgentId();
            }

            if (joinLongFieldBo.getMin() < minValue) {
                minValue = joinLongFieldBo.getMin();
                minAgentId = joinLongFieldBo.getMinAgentId();
            }
        }

        return new JoinLongFieldBo(sumTotalValue / size, minValue, minAgentId, maxValue, maxAgentId);
    }

}

