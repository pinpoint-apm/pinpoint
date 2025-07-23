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
import com.navercorp.pinpoint.common.server.bo.serializer.agent.ApplicationNameRowKeyEncoder;
import com.navercorp.pinpoint.common.server.scatter.FuzzyRowKeyFactory;
import com.navercorp.pinpoint.common.server.scatter.OneByteFuzzyRowKeyFactory;

import java.util.Objects;

public class ApplicationIndexRowKeyEncoder implements RowKeyEncoder<SpanBo> {

    private final ApplicationNameRowKeyEncoder rowKeyEncoder;
    private final FuzzyRowKeyFactory<Byte> fuzzyRowKeyFactory = new OneByteFuzzyRowKeyFactory();
    private final ByteHasher hasher;

    public ApplicationIndexRowKeyEncoder(ApplicationNameRowKeyEncoder rowKeyEncoder,
                                         RowKeyDistributor rowKeyDistributor) {
        this.rowKeyEncoder = Objects.requireNonNull(rowKeyEncoder, "rowKeyEncoder");
        Objects.requireNonNull(rowKeyDistributor, "rowKeyDistributor");
        this.hasher = rowKeyDistributor.getByteHasher();
    }

    public byte[] encodeRowKey(int saltKeySize, String applicationName, int elapsedTime, long acceptedTime) {
        // distribute key evenly
        byte fuzzyKey = fuzzyRowKeyFactory.getKey(elapsedTime);
        final byte[] rowKey = rowKeyEncoder.encodeFuzzyRowKey(saltKeySize, applicationName, acceptedTime, fuzzyKey);
        if (saltKeySize == 0) {
            return rowKey;
        }
        return hasher.writeSaltKey(rowKey);
    }

    @Override
    public byte[] encodeRowKey(SpanBo span) {
        // distribute key evenly
        return encodeRowKey(hasher.getSaltKey().size(), span.getApplicationName(), span.getElapsed(), span.getCollectorAcceptTime());
    }

    @Override
    public byte[] encodeRowKey(int saltKey, SpanBo span) {
        return encodeRowKey(saltKey, span.getApplicationName(), span.getElapsed(), span.getCollectorAcceptTime());
    }
}
