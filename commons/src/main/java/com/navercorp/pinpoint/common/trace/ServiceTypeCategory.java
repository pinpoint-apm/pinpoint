/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.common.trace;

import java.util.Objects;

/**
 * @author Jongho Moon <jongho.moon@navercorp.com>
 *
 */
public enum ServiceTypeCategory {
    UNDEFINED_CATEGORY(-1, -1, NodeCategory.UNDEFINED),
    PINPOINT_INTERNAL(0, 999, NodeCategory.UNDEFINED),
    SERVER(1000, 1999, NodeCategory.SERVER),
    DATABASE(2000, 2999, NodeCategory.DATABASE),
    LIBRARY(5000, 7999, NodeCategory.UNDEFINED),
    CACHE_LIBRARY(8000, 8299, NodeCategory.CACHE, HistogramSchemas.FAST_SCHEMA),
    MESSAGE_BROKER(8300, 8799, NodeCategory.MESSAGE_BROKER),
    HBASE(8800, 8899, NodeCategory.DATABASE),
    CACHE_LIBRARY_SANDBOX(8900, 8999, NodeCategory.UNDEFINED, HistogramSchemas.FAST_SCHEMA),
    RPC(9000, 9999, NodeCategory.UNKNOWN);


    private final int minCode;
    private final int maxCode;

    private final NodeCategory nodeCategory;

    private final HistogramSchema histogramSchema;

    private static final ServiceTypeCategory[] SERVICE_TYPE_CATEGORIES = ServiceTypeCategory.values();

    ServiceTypeCategory(int minCode, int maxCode, NodeCategory nodeCategory) {
        this(minCode, maxCode, nodeCategory, HistogramSchemas.NORMAL_SCHEMA);
    }

    ServiceTypeCategory(int minCode, int maxCode, NodeCategory nodeCategory, HistogramSchema histogramSchema) {
        this.minCode = minCode;
        this.maxCode = maxCode;
        this.nodeCategory = nodeCategory;
        this.histogramSchema = Objects.requireNonNull(histogramSchema, "histogramSchema");
    }

    public NodeCategory nodeCategory() {
        return nodeCategory;
    }

    public boolean contains(int code) {
        return minCode <= code && code <= maxCode;
    }

    public boolean contains(ServiceType type) {
        return contains(type.getCode());
    }

    public HistogramSchema getHistogramSchema() {
        return histogramSchema;
    }

    public static ServiceTypeCategory findCategory(int code) {
        for (ServiceTypeCategory serviceTypeCategory : SERVICE_TYPE_CATEGORIES) {
            if (serviceTypeCategory.contains(code)) {
                return serviceTypeCategory;
            }
        }
        throw new IllegalArgumentException("Unknown Category code:" + code);
    }
}
