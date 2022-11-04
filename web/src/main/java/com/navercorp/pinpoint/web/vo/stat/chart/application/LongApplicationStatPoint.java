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
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.web.view.LongApplicationStatSerializer;
import com.navercorp.pinpoint.web.vo.chart.Point;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
@JsonSerialize(using = LongApplicationStatSerializer.class)
public class LongApplicationStatPoint implements Point {

    public static final long UNCOLLECTED_VALUE = -1L;

    private final long xVal;
    private final JoinLongFieldBo longFieldBo;


    public LongApplicationStatPoint(long xVal, JoinLongFieldBo longFieldBo) {
        this.xVal = xVal;
        this.longFieldBo = Objects.requireNonNull(longFieldBo, "longFieldBo");
    }

    @Override
    public long getTimestamp() {
        return xVal;
    }

    public JoinLongFieldBo getLongFieldBo() {
        return longFieldBo;
    }


    @Override
    public String toString() {
        return "LongApplicationStatPoint{" +
                "xVal=" + xVal +
                ", longFieldBo=" + longFieldBo +
                '}';
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
            JoinLongFieldBo empty = new JoinLongFieldBo(uncollectedValue, uncollectedValue, JoinStatBo.UNKNOWN_AGENT, uncollectedValue, JoinStatBo.UNKNOWN_AGENT);
            return new LongApplicationStatPoint(xVal, empty);
        }

    }

}
