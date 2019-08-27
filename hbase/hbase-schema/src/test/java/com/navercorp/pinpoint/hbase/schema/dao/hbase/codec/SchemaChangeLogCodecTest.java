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

import com.navercorp.pinpoint.hbase.schema.core.CheckSum;
import com.navercorp.pinpoint.hbase.schema.domain.SchemaChangeLog;
import org.junit.Test;

import java.util.Random;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author HyunGil Jeong
 */
public class SchemaChangeLogCodecTest {

    private final Random random = new Random();

    private final SchemaChangeLogCodec schemaChangeLogCodec = new SchemaChangeLogCodec();

    @Test
    public void writeAndRead() {
        final String value = UUID.randomUUID().toString();
        SchemaChangeLog schemaChangeLog = new SchemaChangeLog.Builder()
                .id("testId")
                .execTimestamp(System.currentTimeMillis())
                .execOrder(random.nextInt())
                .checkSum(CheckSum.compute(CheckSum.getCurrentVersion(), value))
                .value(UUID.randomUUID().toString())
                .build();
        byte[] serialized = schemaChangeLogCodec.writeData(schemaChangeLog);
        SchemaChangeLog deserialized = schemaChangeLogCodec.readData(serialized);
        assertThat(deserialized.getId(), is(schemaChangeLog.getId()));
        assertThat(deserialized.getExecTimestamp(), is(schemaChangeLog.getExecTimestamp()));
        assertThat(deserialized.getExecOrder(), is(schemaChangeLog.getExecOrder()));
        assertThat(deserialized.getCheckSum(), is(schemaChangeLog.getCheckSum()));
        assertThat(deserialized.getValue(), is(schemaChangeLog.getValue()));
    }
}
