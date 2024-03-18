/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.collector.dao.hbase.encode;

import com.navercorp.pinpoint.common.id.ApplicationId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.agent.ApplicationNameRowKeyEncoder;
import com.navercorp.pinpoint.common.server.scatter.FuzzyRowKeyFactory;
import com.navercorp.pinpoint.common.server.scatter.OneByteFuzzyRowKeyFactory;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class ApplicationIndexRowKeyEncoderV2 implements RowKeyEncoder<SpanBo> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationNameRowKeyEncoder rowKeyEncoder = new ApplicationNameRowKeyEncoder();
    private final FuzzyRowKeyFactory<Byte> fuzzyRowKeyFactory = new OneByteFuzzyRowKeyFactory();
    private final AbstractRowKeyDistributor rowKeyDistributor;

    public ApplicationIndexRowKeyEncoderV2(AbstractRowKeyDistributor rowKeyDistributor) {
        this.rowKeyDistributor = Objects.requireNonNull(rowKeyDistributor, "rowKeyDistributor");
    }

    @Override
    public byte[] encodeRowKey(SpanBo span) {
        // distribute key evenly
        long acceptedTime = span.getCollectorAcceptTime();
        byte fuzzyKey = fuzzyRowKeyFactory.getKey(span.getElapsed());
        final byte[] appTraceIndexRowKey = newRowKey(span.getApplicationId(), acceptedTime, fuzzyKey);
        return rowKeyDistributor.getDistributedKey(appTraceIndexRowKey);
    }

    byte[] newRowKey(ApplicationId applicationId, long acceptedTime, byte fuzzySlotKey) {
        Objects.requireNonNull(applicationId, "applicationId");

        if (logger.isDebugEnabled()) {
            logger.debug("fuzzySlotKey:{}", fuzzySlotKey);
        }
        byte[] rowKey = rowKeyEncoder.encodeRowKey(ApplicationId.unwrap(applicationId), acceptedTime);

        byte[] fuzzyRowKey = new byte[rowKey.length + 1];
        System.arraycopy(rowKey, 0, fuzzyRowKey, 0, rowKey.length);

        fuzzyRowKey[rowKey.length] = fuzzySlotKey;
        return fuzzyRowKey;
    }
}
