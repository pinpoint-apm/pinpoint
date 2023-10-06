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

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.agent.ApplicationNameRowKeyEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.agent.IdRowKeyEncoder;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;

import java.util.Objects;

public class ApplicationIndexRowKeyEncoderV1 implements RowKeyEncoder<SpanBo> {

    private final IdRowKeyEncoder rowKeyEncoder = new ApplicationNameRowKeyEncoder();
    private final AbstractRowKeyDistributor rowKeyDistributor;
    private final AcceptedTimeService acceptedTimeService;

    public ApplicationIndexRowKeyEncoderV1(AbstractRowKeyDistributor rowKeyDistributor, AcceptedTimeService acceptedTimeService) {
        this.rowKeyDistributor = Objects.requireNonNull(rowKeyDistributor, "rowKeyDistributor");
        this.acceptedTimeService = Objects.requireNonNull(acceptedTimeService, "acceptedTimeService");
    }

    @Override
    public byte[] encodeRowKey(SpanBo span) {
        // distribute key evenly
        long acceptedTime = acceptedTimeService.getAcceptedTime();
        final byte[] applicationTraceIndexRowKey = rowKeyEncoder.encodeRowKey(span.getApplicationId(), acceptedTime);
        return rowKeyDistributor.getDistributedKey(applicationTraceIndexRowKey);
    }
}
