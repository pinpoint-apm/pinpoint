/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.dao.hbase.encode;

import com.navercorp.pinpoint.common.hbase.wd.ByteHasher;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributor;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.scatter.FuzzyRowKeyFactory;
import com.navercorp.pinpoint.common.server.scatter.OneByteFuzzyRowKeyFactory;
import com.navercorp.pinpoint.common.server.scatter.TraceIndexRowKey;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.Objects;

public class TraceIndexRowKeyEncoder implements RowKeyEncoder<SpanBo> {

    private final FuzzyRowKeyFactory<Byte> fuzzyRowKeyFactory = new OneByteFuzzyRowKeyFactory();
    private final ByteHasher hasher;

    public TraceIndexRowKeyEncoder(RowKeyDistributor rowKeyDistributor) {
        Objects.requireNonNull(rowKeyDistributor, "rowKeyDistributor");
        this.hasher = rowKeyDistributor.getByteHasher();
    }

    public byte[] encodeRowKey(int saltKeySize, int serviceUid, String applicationName, int serviceTypeCode, int elapsedTime, long acceptedTime, long spanId) {
        byte fuzzySlotKey = fuzzyRowKeyFactory.getKey(elapsedTime);
        final byte[] rowKey = TraceIndexRowKey.createFuzzyRowKey(saltKeySize, serviceUid, applicationName, serviceTypeCode, acceptedTime, fuzzySlotKey, spanId);
        if (saltKeySize == 0) {
            return rowKey;
        }
        return hasher.writeSaltKey(rowKey);
    }

    @Override
    public byte[] encodeRowKey(SpanBo span) {
        ServiceUid serviceUid = ServiceUid.DEFAULT; //span.getServiceUid();
        return encodeRowKey(hasher.getSaltKey().size(), serviceUid.getUid(), span.getApplicationName(), span.getApplicationServiceType(), span.getElapsed(), span.getCollectorAcceptTime(), span.getSpanId());
    }

    @Override
    public byte[] encodeRowKey(int saltKeySize, SpanBo span) {
        ServiceUid serviceUid = ServiceUid.DEFAULT; //span.getServiceUid();
        return encodeRowKey(saltKeySize, serviceUid.getUid(), span.getApplicationName(), span.getApplicationServiceType(), span.getElapsed(), span.getCollectorAcceptTime(), span.getSpanId());
    }
}
