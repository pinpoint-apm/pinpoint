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

package com.navercorp.pinpoint.common.hbase;


import com.navercorp.pinpoint.common.hbase.util.CheckAndMutates;
import org.apache.hadoop.hbase.client.CheckAndMutate;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.Objects;

public record CheckAndMax(byte[] row, byte[] family, byte[] qualifier, long value) {
    public CheckAndMax {
        Objects.requireNonNull(row, "row");
        Objects.requireNonNull(family, "family");
        Objects.requireNonNull(qualifier, "qualifier");
    }

    public static CheckAndMutate initialMax(CheckAndMax max) {
        Put put = new Put(max.row(), true);
        put.addColumn(max.family(), max.qualifier(), Bytes.toBytes(max.value()));

        return CheckAndMutate.newBuilder(max.row())
                .ifNotExists(max.family(), max.qualifier())
                .build(put);
    }

    public static CheckAndMutate casMax(CheckAndMutate mutate) {
        Objects.requireNonNull(mutate, "mutate");
        return CheckAndMutates.max(mutate.getRow(), mutate.getFamily(), mutate.getQualifier(), mutate.getValue(), (Put) mutate.getAction());
    }

}
