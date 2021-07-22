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

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Taejin Koo
 */
public class JoinIntFieldBo extends AbstractJoinFieldBo<Integer> {

    private static final int UNCOLLECTED_VALUE = -1;
    static final JoinIntFieldBo UNCOLLECTED_FIELD_BO = new JoinIntFieldBo(UNCOLLECTED_VALUE, UNCOLLECTED_VALUE, JoinStatBo.UNKNOWN_AGENT, UNCOLLECTED_VALUE, JoinStatBo.UNKNOWN_AGENT);

    public JoinIntFieldBo(Integer avgValue, Integer minValue, String minAgentId, Integer maxValue, String maxAgentId) {
        super(avgValue, minValue, minAgentId, maxValue, maxAgentId);
    }

    @Override
    protected JoinFieldBo<Integer> getUncollectedValue() {
        return UNCOLLECTED_FIELD_BO;
    }

    protected static JoinIntFieldBo merge(List<JoinIntFieldBo> joinIntFieldBoList) {
        if (CollectionUtils.isEmpty(joinIntFieldBoList)) {
            return UNCOLLECTED_FIELD_BO;
        }

        double avg = joinIntFieldBoList.stream()
                .mapToInt(JoinFieldBo::getAvg)
                .average()
                .orElseThrow(NoSuchElementException::new);
        JoinIntFieldBo max = joinIntFieldBoList.stream()
                .max(Comparator.comparing(JoinFieldBo::getMax))
                .orElseThrow(NoSuchElementException::new);
        JoinIntFieldBo min = joinIntFieldBoList.stream()
                .min(Comparator.comparing(JoinFieldBo::getMin))
                .orElseThrow(NoSuchElementException::new);

        String maxAgentId = max.getMaxAgentId();
        int maxValue = max.getMax();

        String minAgentId = min.getMinAgentId();
        int minValue = min.getMin();

        return new JoinIntFieldBo((int) avg, minValue, minAgentId, maxValue, maxAgentId);
    }

}

