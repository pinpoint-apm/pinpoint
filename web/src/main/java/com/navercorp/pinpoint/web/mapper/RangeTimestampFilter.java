/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.web.vo.Range;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class RangeTimestampFilter implements TimestampFilter {

    private final Range range;

    public RangeTimestampFilter(Range range) {
        this.range = Objects.requireNonNull(range, "range");
    }

    @Override
    public boolean filter(long timestamp) {
        return timestamp < this.range.getFrom() || timestamp > this.range.getTo();
    }
}
