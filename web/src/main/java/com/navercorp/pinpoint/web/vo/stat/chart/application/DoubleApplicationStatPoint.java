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
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDoubleFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.web.view.DoubleApplicationStatSerializer;
import com.navercorp.pinpoint.web.vo.chart.Point;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
@JsonSerialize(using = DoubleApplicationStatSerializer.class)
public class DoubleApplicationStatPoint implements Point {

    public static final double UNCOLLECTED_VALUE = -1D;

    private final long xVal;

    private final JoinDoubleFieldBo doubleFieldBo;

    public DoubleApplicationStatPoint(long xVal, JoinDoubleFieldBo doubleFieldBo) {
        this.xVal = xVal;
        this.doubleFieldBo = Objects.requireNonNull(doubleFieldBo, "doubleFieldBo");
    }

    @Override
    public long getTimestamp() {
        return xVal;
    }

    public JoinDoubleFieldBo getDoubleFieldBo() {
        return doubleFieldBo;
    }

    @Override
    public String toString() {
        return "DoubleApplicationStatPoint{" +
                "xVal=" + xVal +
                ", doubleFieldBo=" + doubleFieldBo +
                '}';
    }

    public static class UncollectedCreator implements UncollectedPointCreator<DoubleApplicationStatPoint> {

        private final double uncollectedValue;

        public UncollectedCreator() {
            this(UNCOLLECTED_VALUE);
        }

        public UncollectedCreator(double uncollectedValue) {
            this.uncollectedValue = uncollectedValue;
        }

        @Override
        public DoubleApplicationStatPoint createUnCollectedPoint(long xVal) {
            JoinDoubleFieldBo empty = new JoinDoubleFieldBo(uncollectedValue, uncollectedValue, JoinStatBo.UNKNOWN_AGENT, uncollectedValue, JoinStatBo.UNKNOWN_AGENT);
            return new DoubleApplicationStatPoint(xVal, empty);
        }

    }

}
