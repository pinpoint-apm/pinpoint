/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.hbase.schema.dao.hbase.codec;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.hbase.schema.core.CheckSum;
import com.navercorp.pinpoint.hbase.schema.domain.SchemaChangeLog;

/**
 * @author HyunGil Jeong
 */
public class SchemaChangeLogCodec implements HbaseValueCodec<SchemaChangeLog> {

    private static final short CURRENT_VERSION = 1;

    @Override
    public byte[] writeData(SchemaChangeLog schemaChangeLog) {
        final Buffer buffer = new AutomaticBuffer();
        buffer.putShort(CURRENT_VERSION);
        buffer.putPrefixedString(schemaChangeLog.getId());
        buffer.putLong(schemaChangeLog.getExecTimestamp());
        buffer.putInt(schemaChangeLog.getExecOrder());
        CheckSum checkSum = schemaChangeLog.getCheckSum();
        buffer.putInt(checkSum.getVersion());
        buffer.putPrefixedString(checkSum.getCheckSum());
        buffer.putPrefixedString(schemaChangeLog.getValue());
        return buffer.getBuffer();
    }

    @Override
    public SchemaChangeLog readData(byte[] data) {
        if (data == null) {
            return null;
        }
        final Buffer buffer = new FixedBuffer(data);
        short version = buffer.readShort();
        if (version == 1) {
            String id = buffer.readPrefixedString();
            long execTimestamp = buffer.readLong();
            int execOrder = buffer.readInt();
            CheckSum checkSum = new CheckSum(buffer.readInt(), buffer.readPrefixedString());
            String value = buffer.readPrefixedString();
            return new SchemaChangeLog.Builder()
                    .id(id)
                    .execTimestamp(execTimestamp)
                    .execOrder(execOrder)
                    .checkSum(checkSum)
                    .value(value)
                    .build();
        }
        throw new IllegalStateException("Unsupported schema change log version : " + version);
    }
}
