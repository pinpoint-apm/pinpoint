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

package com.navercorp.pinpoint.web.vo.stat.chart.application;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.web.view.LongApplicationStatSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Taejin Koo
 */
@JsonSerialize(using = LongApplicationStatSerializer.class)
public class LongApplicationStatPoint extends ApplicationStatPoint<Long> {

    public static final long UNCOLLECTED_VALUE = -1L;

    public LongApplicationStatPoint(long xVal, Long yValForMin, String agentIdForMin, Long yValForMax, String agentIdForMax, Long yValForAvg) {
        super(xVal, yValForMin, agentIdForMin, yValForMax, agentIdForMax, yValForAvg);
    }

    public static class UncollectedCreator implements UncollectedPointCreator<LongApplicationStatPoint> {

        private final long uncollectedValue;

        public UncollectedCreator() {
            this(UNCOLLECTED_VALUE);
        }

        public UncollectedCreator(long uncollectedValue) {
            this.uncollectedValue = uncollectedValue;
        }

        @Override
        public LongApplicationStatPoint createUnCollectedPoint(long xVal) {
            return new LongApplicationStatPoint(xVal, uncollectedValue,
                    JoinStatBo.UNKNOWN_AGENT, uncollectedValue,
                    JoinStatBo.UNKNOWN_AGENT, uncollectedValue);
        }

    }

}
