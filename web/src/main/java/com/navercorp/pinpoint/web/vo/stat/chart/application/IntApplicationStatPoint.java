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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinIntFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.web.view.IntApplicationStatSerializer;
import com.navercorp.pinpoint.web.vo.chart.Point;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
@JsonSerialize(using = IntApplicationStatSerializer.class)
public class IntApplicationStatPoint implements Point {

    public static final int UNCOLLECTED_VALUE = -1;

    private final long xVal;
    private final JoinIntFieldBo intFieldBo;

    public IntApplicationStatPoint(long xVal, JoinIntFieldBo intFieldBo) {
        this.xVal = xVal;
        this.intFieldBo = Objects.requireNonNull(intFieldBo, "intFieldBo");
    }

    @Override
    public long getTimestamp() {
        return xVal;
    }

    public JoinIntFieldBo getIntFieldBo() {
        return intFieldBo;
    }

    @Override
    public String toString() {
        return "IntApplicationStatPoint{" +
                "xVal=" + xVal +
                ", intFieldBo=" + intFieldBo +
                '}';
    }

    public static class UncollectedCreator implements UncollectedPointCreator<IntApplicationStatPoint> {

        private final int uncollectedValue;

        public UncollectedCreator() {
            this(UNCOLLECTED_VALUE);
        }

        public UncollectedCreator(int uncollectedValue) {
            this.uncollectedValue = uncollectedValue;
        }

        @Override
        public IntApplicationStatPoint createUnCollectedPoint(long xVal) {
            JoinIntFieldBo empty = new JoinIntFieldBo(uncollectedValue, uncollectedValue, JoinStatBo.UNKNOWN_AGENT, uncollectedValue, JoinStatBo.UNKNOWN_AGENT);
            return new IntApplicationStatPoint(xVal, empty);
        }

    }

}
