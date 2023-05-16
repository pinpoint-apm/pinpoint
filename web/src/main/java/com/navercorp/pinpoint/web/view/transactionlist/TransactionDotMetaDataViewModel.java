/*
 * Copyright 2019 NAVER Corp.
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
package com.navercorp.pinpoint.web.view.transactionlist;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import com.navercorp.pinpoint.web.vo.scatter.DotMetaData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class TransactionDotMetaDataViewModel {

    private final List<DotMetaData> dotMetaDataList;

    public TransactionDotMetaDataViewModel(List<DotMetaData> dotMetaDataList) {
        this.dotMetaDataList = Objects.requireNonNull(dotMetaDataList, "dotMetaDataList");
    }

    @JsonProperty("metadata")
    public List<DotMetaDataView> getMetadata() {
        List<DotMetaDataView> list = new ArrayList<>(dotMetaDataList.size());
        for (DotMetaData span : dotMetaDataList) {
            list.add(new MetaData(span));
        }

        return list;
    }

    public static class MetaData implements DotMetaDataView {
        private final DotMetaData dotMetaData;

        public MetaData(DotMetaData dotMetaData) {
            this.dotMetaData = Objects.requireNonNull(dotMetaData, "dotMetaData");
        }

        @Override
        public String getTraceId() {
            return TransactionIdUtils.formatString(dotMetaData.getDot().getTransactionId());
        }

        @Override
        public long getCollectorAcceptTime() {
            return dotMetaData.getDot().getAcceptedTime();
        }

        @Override
        public long getStartTime() {
            return dotMetaData.getStartTime();
        }

        @Override
        public long getElapsed() {
            return dotMetaData.getDot().getElapsedTime();
        }

        @Override
        public String getApplication() {
            return dotMetaData.getRpc();
        }

        @Override
        public String getAgentId() {
            return dotMetaData.getDot().getAgentId();
        }

        @Override
        public String getAgentName() {
            return dotMetaData.getAgentName();
        }

        @Override
        public String getEndpoint() {
            return dotMetaData.getEndpoint();
        }

        @Override
        public int getException() {
            return dotMetaData.getDot().getExceptionCode();
        }

        @Override
        public String getRemoteAddr() {
            return dotMetaData.getRemoteAddr();
        }

        @Override
        public String getSpanId() {
            return Long.toString(dotMetaData.getSpanId());
        }
    }
}
