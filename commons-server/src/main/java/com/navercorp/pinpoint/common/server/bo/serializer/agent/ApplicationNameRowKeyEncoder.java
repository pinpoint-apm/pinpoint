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

package com.navercorp.pinpoint.common.server.bo.serializer.agent;

import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.server.util.RowKeyUtils;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;

public class ApplicationNameRowKeyEncoder extends IdRowKeyEncoder {

    public ApplicationNameRowKeyEncoder() {
        super(HbaseTableConstants.APPLICATION_NAME_MAX_LEN);
    }

    @Override
    public byte[] encodeRowKey(String applicationName, long timestamp) {
        return super.encodeRowKey(applicationName, timestamp);
    }

    public byte[] encodeFuzzyRowKey(String id, long timestamp, byte fuzzySlotKey) {
        byte[] idKey = BytesUtils.toBytes(id);
        long reverseTimestamp = TimeUtils.reverseTimeMillis(timestamp);
        return RowKeyUtils.concatFixedByteAndLongFuzzySlot(idKey, max, reverseTimestamp, fuzzySlotKey);
    }
}
