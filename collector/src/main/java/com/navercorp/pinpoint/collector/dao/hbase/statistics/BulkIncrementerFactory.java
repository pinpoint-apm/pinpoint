/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.collector.dao.hbase.statistics;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public final class BulkIncrementerFactory {

    public BulkIncrementer create(RowKeyMerge rowKeyMerge) {
        return create(rowKeyMerge, Integer.MAX_VALUE);
    }

    public BulkIncrementer create(RowKeyMerge rowKeyMerge, int limitSize) {
        if (hasLimit(limitSize)) {
            return new BulkIncrementer.SizeLimitedBulkIncrementer(rowKeyMerge, limitSize);
        } else {
            return new BulkIncrementer.DefaultBulkIncrementer(rowKeyMerge);
        }
    }

    private boolean hasLimit(int limitSize) {
        if (limitSize > 0) {
            if (limitSize != Integer.MAX_VALUE) {
                return true;
            }
        }
        return false;
    }

}
