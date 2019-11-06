/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.vo.stat.chart.agent;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.view.TitledAgentStatPointSerializer;

/**
 * @author HyunGil Jeong
 */
@JsonSerialize(using = TitledAgentStatPointSerializer.class)
public class TitledAgentStatPoint<Y extends Number> extends AgentStatPoint<Y> {

    private final String title;

    public TitledAgentStatPoint(String title, long xVal, Y yVal) {
        super(xVal, yVal);
        this.title = title;
    }

    public TitledAgentStatPoint(String title, long xVal, Y minYVal, Y maxYVal, Double avgYVal, Y sumYVal) {
        super(xVal, minYVal, maxYVal, avgYVal, sumYVal);
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TitledAgentStatPoint<?> that = (TitledAgentStatPoint<?>) o;

        return title != null ? title.equals(that.title) : that.title == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (title != null ? title.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TitledAgentStatPoint{");
        sb.append("title='").append(title).append('\'');
        sb.append(", ").append(super.toString());
        return sb.toString();
    }
}
