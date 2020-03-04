/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.hbase.schema.domain;

import com.navercorp.pinpoint.hbase.schema.core.CheckSum;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class SchemaChangeLog {

    private final String id;
    private final long execTimestamp;
    private final int execOrder;
    private final CheckSum checkSum;
    private final String value;

    private SchemaChangeLog(Builder builder) {
        this.id = builder.id;
        this.execTimestamp = builder.execTimestamp;
        this.execOrder = builder.execOrder;
        this.checkSum = builder.checkSum;
        this.value = builder.value;
    }

    public String getId() {
        return id;
    }

    public long getExecTimestamp() {
        return execTimestamp;
    }

    public int getExecOrder() {
        return execOrder;
    }

    public CheckSum getCheckSum() {
        return checkSum;
    }

    public String getValue() {
        return value;
    }

    public static class Builder {
        private String id;
        private long execTimestamp;
        private int execOrder;
        private CheckSum checkSum;
        private String value;

        public Builder id(String id) {
            if (StringUtils.isEmpty(id)) {
                throw new IllegalArgumentException("id must not be empty");
            }
            this.id = id;
            return this;
        }

        public Builder execTimestamp(long execTimestamp) {
            this.execTimestamp = execTimestamp;
            return this;
        }

        public Builder execOrder(int execOrder) {
            this.execOrder = execOrder;
            return this;
        }

        public Builder checkSum(CheckSum checkSum) {
            this.checkSum = Objects.requireNonNull(checkSum, "checkSum");
            return this;
        }

        public Builder value(String value) {
            this.value = Objects.requireNonNull(value, "value");
            return this;
        }

        public SchemaChangeLog build() {
            return new SchemaChangeLog(this);
        }
    }
}
