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

import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.List;

/**
 * @author Taejin Koo
 */
public class JoinIntFieldBo extends AbstractJoinFieldBo<Integer> {

    private static final int UNCOLLECTED_VALUE = -1;
    static final JoinIntFieldBo UNCOLLECTED_FIELD_BO = new JoinIntFieldBo(UNCOLLECTED_VALUE, UNCOLLECTED_VALUE, JoinStatBo.UNKNOWN_AGENT, UNCOLLECTED_VALUE, JoinStatBo.UNKNOWN_AGENT);

    public JoinIntFieldBo(Integer value, Integer minValue, String minAgentId, Integer maxValue, String maxAgentid) {
        super(value, minValue, minAgentId, maxValue, maxAgentid);
    }

    @Override
    protected JoinFieldBo<Integer> getUncollectedValue() {
        return UNCOLLECTED_FIELD_BO;
    }

    protected static JoinIntFieldBo merge(List<JoinIntFieldBo> joinIntFieldBoList) {
        final int size = CollectionUtils.nullSafeSize(joinIntFieldBoList);
        if (size == 0) {
            return UNCOLLECTED_FIELD_BO;
        }

        JoinFieldBo<Integer> firstIntJoinFieldBo = joinIntFieldBoList.get(0);
        int sumTotalValue = 0;

        String maxAgentId = firstIntJoinFieldBo.getMaxAgentId();
        int maxValue = firstIntJoinFieldBo.getMax();

        String minAgentId = firstIntJoinFieldBo.getMinAgentId();
        int minValue = firstIntJoinFieldBo.getMin();

        for (JoinFieldBo<Integer> joinIntFieldBo : joinIntFieldBoList) {
            sumTotalValue += joinIntFieldBo.getAvg();

            if (joinIntFieldBo.getMax() > maxValue) {
                maxValue = joinIntFieldBo.getMax();
                maxAgentId = joinIntFieldBo.getMaxAgentId();
            }

            if (joinIntFieldBo.getMin() < minValue) {
                minValue = joinIntFieldBo.getMin();
                minAgentId = joinIntFieldBo.getMinAgentId();
            }
        }

        return new JoinIntFieldBo(sumTotalValue / size, minValue, minAgentId, maxValue, maxAgentId);
    }

}

