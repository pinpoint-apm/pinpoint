/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.exceptiontrace.web.query;

import com.navercorp.pinpoint.exceptiontrace.web.util.ClpReplacedColumns;
import com.navercorp.pinpoint.metric.web.util.QueryParameter;

/**
 * @author intr3p1d
 */
public class ClpQueryParameter extends QueryParameter {

    private final String tableName;
    private final String tenantId;
    private final String applicationName;
    private final String agentId;

    private final ClpReplacedColumns targetColumn;
    private final int targetIndex;
    private final String logType;

    protected ClpQueryParameter(Builder builder) {
        super(builder.getRange(), builder.getTimePrecision(), builder.getLimit());
        this.tableName = builder.tableName;
        this.tenantId = builder.tenantId;
        this.applicationName = builder.applicationName;
        this.agentId = builder.agentId;
        this.targetColumn = builder.targetColumn;
        this.targetIndex = builder.targetIndex;
        this.logType = builder.logType;
    }

    public static class Builder extends QueryParameter.Builder<Builder> {

        private String tableName;
        private String tenantId;
        private String applicationName;
        private String agentId;

        private ClpReplacedColumns targetColumn;
        private int targetIndex;
        private String logType;

        @Override
        protected Builder self() {
            return this;
        }


        public Builder setTableName(String tableName) {
            this.tableName = tableName;
            return self();
        }

        public Builder setTenantId(String tenantId) {
            this.tenantId = tenantId;
            return self();
        }

        public Builder setApplicationName(String applicationName) {
            this.applicationName = applicationName;
            return self();
        }

        public Builder setAgentId(String agentId) {
            this.agentId = agentId;
            return self();
        }

        public Builder setTargetColumn(String targetColumn) {
            this.targetColumn = ClpReplacedColumns.fromValue(targetColumn);
            return self();
        }

        public Builder setTargetIndex(int targetIndex) {
            this.targetIndex = targetIndex;
            return self();
        }

        public Builder setLogType(String logType) {
            this.logType = logType;
            return self();
        }

        @Override
        public ClpQueryParameter build() {
            return new ClpQueryParameter(this);
        }
    }


    @Override
    public String toString() {
        return "ClpQueryParameter{" +
                "tableName='" + tableName + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", agentId='" + agentId + '\'' +
                ", targetColumn=" + targetColumn +
                ", targetIndex=" + targetIndex +
                ", logType='" + logType + '\'' +
                '}';
    }
}
