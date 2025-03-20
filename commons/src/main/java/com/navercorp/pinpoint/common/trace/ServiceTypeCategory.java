/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.common.trace;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Jongho Moon <jongho.moon@navercorp.com>
 *
 */
public enum ServiceTypeCategory {
    UNDEFINED_CATEGORY(-1, -1),
    PINPOINT_INTERNAL(0, 999),
    SERVER(1000, 1999),
    DATABASE(2000, 2999),
    LIBRARY(5000, 7999),
    CACHE_LIBRARY(8000, 8299, BaseHistogramSchema.FAST_SCHEMA),
    MESSAGE_BROKER(8300, 8799),
    HBASE(8800, 8899),
    CACHE_LIBRARY_SANDBOX(8900, 8999, BaseHistogramSchema.FAST_SCHEMA),
    RPC(9000, 9999);


    private final int minCode;
    private final int maxCode;
    private final HistogramSchema histogramSchema;

    private static final Set<ServiceTypeCategory> SERVICE_TYPE_CATEGORIES = EnumSet.allOf(ServiceTypeCategory.class);

    ServiceTypeCategory(int minCode, int maxCode) {
        this(minCode, maxCode, BaseHistogramSchema.NORMAL_SCHEMA);
    }

    ServiceTypeCategory(int minCode, int maxCode, HistogramSchema histogramSchema) {
        this.minCode = minCode;
        this.maxCode = maxCode;
        this.histogramSchema = Objects.requireNonNull(histogramSchema, "histogramSchema");
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
