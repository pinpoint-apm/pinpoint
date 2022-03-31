/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.hbase.bo.ColumnGetCount;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.common.server.util.time.Range;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class FilteredMapServiceOption {
    private final List<TransactionId> transactionIdList;
    private final Range originalRange;
    private final Range scanRange;
    private final int xGroupUnit;
    private final int yGroupUnit;
    private final Filter<List<SpanBo>> filter;
    private final int version;
    private final boolean useStatisticsAgentState;
    private final ColumnGetCount columnGetCount;

    public FilteredMapServiceOption(final Builder builder) {
        this.transactionIdList = builder.transactionIdList;
        this.originalRange = builder.originalRange;
//        this.scanRange = builder.scanRange;
        this.scanRange = null;
        this.xGroupUnit = builder.xGroupUnit;
        this.yGroupUnit = builder.yGroupUnit;
        this.filter = builder.filter;
        this.version = builder.version;
        this.useStatisticsAgentState = builder.useStatisticsAgentState;
        this.columnGetCount = builder.columnGetCount;
    }

    public List<TransactionId> getTransactionIdList() {
        return transactionIdList;
    }

    public Range getOriginalRange() {
        return originalRange;
    }

    public Range getScanRange() {
        return scanRange;
    }

    public int getxGroupUnit() {
        return xGroupUnit;
    }

    public int getyGroupUnit() {
        return yGroupUnit;
    }

    public Filter<List<SpanBo>> getFilter() {
        return filter;
    }

    public int getVersion() {
        return version;
    }

    public boolean isUseStatisticsAgentState() {
        return useStatisticsAgentState;
    }

    public ColumnGetCount getColumnGetCount() {
        return columnGetCount;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FilteredMapServiceOption{");
        sb.append("transactionIdList=").append(transactionIdList);
        sb.append(", originalRange=").append(originalRange);
        sb.append(", scanRange=").append(scanRange);
        sb.append(", xGroupUnit=").append(xGroupUnit);
        sb.append(", yGroupUnit=").append(yGroupUnit);
        sb.append(", filter=").append(filter);
        sb.append(", version=").append(version);
        sb.append(", useStatisticsAgentState=").append(useStatisticsAgentState);
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {
        private List<TransactionId> transactionIdList;
        private Range originalRange;
        private Range scanRange;
        private int xGroupUnit;
        private int yGroupUnit;
        private Filter<List<SpanBo>> filter;
        private int version;
        private ColumnGetCount columnGetCount;

        private boolean useStatisticsAgentState;

        public Builder(TransactionId transactionId, int version, ColumnGetCount columnGetCount) {
            Objects.requireNonNull(transactionId, "transactionId");
            this.transactionIdList = Collections.singletonList(transactionId);
            this.version = version;
            this.columnGetCount = columnGetCount;
            this.originalRange = Range.between(-1, -1);
            this.filter = Filter.acceptAllFilter();
        }

        public Builder(List<TransactionId> transactionIdList, Range originalRange, Range scanRange, int xGroupUnit, int yGroupUnit, Filter<List<SpanBo>> filter, int version) {
            this.transactionIdList = Objects.requireNonNull(transactionIdList, "transactionIdList");
            this.filter = Objects.requireNonNull(filter, "filter");
            this.originalRange = originalRange;
            this.scanRange = scanRange;
            this.xGroupUnit = xGroupUnit;
            this.yGroupUnit = yGroupUnit;
            this.version = version;
        }

        public Builder setUseStatisticsAgentState(boolean useStatisticsAgentState) {
            this.useStatisticsAgentState = useStatisticsAgentState;
            return this;
        }

        public FilteredMapServiceOption build() {
            return new FilteredMapServiceOption(this);
        }
    }
}
