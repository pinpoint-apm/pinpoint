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

package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.config.DistributorConfiguration;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.MetaDataRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.MetadataDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.MetadataEncoder;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellBuilderFactory;
import org.apache.hadoop.hbase.CellBuilderType;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApiMetaDataMapperTest {
    // from node: ApiMetaDataBo{agentId='express-node-sample-id', startTime=1669280767548, apiId=12, apiInfo='express.Function.proto.get(path, callback)', lineNumber=169, methodTypeEnum=DEFAULT, location='/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/routes/index.js'}
    @Test
    public void testMapRow() throws Exception {
        ApiMetaDataBo expected = new ApiMetaDataBo.Builder("express-node-sample-id", 1669280767548L, 12, 169, MethodTypeEnum.DEFAULT, "express.Function.proto.get(path, callback)")
                .setLocation("/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/routes/index.js")
                .build();

        RowKeyDistributorByHashPrefix givenRowKeyDistributorByHashPrefix = new DistributorConfiguration().metadataRowKeyDistributor();
        final byte[] rowKey = givenRowKeyDistributorByHashPrefix.getDistributedKey(new MetadataEncoder().encodeRowKey(expected));
        final Buffer buffer = new AutomaticBuffer(64);
        final String api = expected.getApiInfo();
        buffer.putPrefixedString(api);
        buffer.putInt(expected.getLineNumber());
        buffer.putInt(expected.getMethodTypeEnum().getCode());

        String location = expected.getLocation();
        if (location != null) {
            buffer.putPrefixedString(location);
        }
        byte[] bufferArray = buffer.getBuffer();
        byte[] valueArray = Bytes.toBytes(1L);

        Cell cell = CellBuilderFactory.create(CellBuilderType.SHALLOW_COPY)
                .setRow(HConstants.EMPTY_BYTE_ARRAY)
                .setFamily(HConstants.EMPTY_BYTE_ARRAY)
                .setQualifier(bufferArray)
                .setTimestamp(HConstants.LATEST_TIMESTAMP)
                .setType(Cell.Type.Put)
                .setValue(valueArray)
                .build();

        Result mockedResult = mock(Result.class);
        when(mockedResult.rawCells()).thenReturn(new Cell[] { cell });
        when(mockedResult.getRow()).thenReturn(rowKey);

        RowKeyDecoder<MetaDataRowKey> decoder = new MetadataDecoder();
        ApiMetaDataMapper dut = new ApiMetaDataMapper(decoder, givenRowKeyDistributorByHashPrefix);
        ApiMetaDataBo actual = dut.mapRow(mockedResult, 0).get(0);

        assertThat(actual).extracting("agentId", "startTime", "apiId", "apiInfo", "lineNumber", "methodTypeEnum", "location")
                .contains(expected.getAgentId(), expected.getAgentStartTime(), expected.getId(), expected.getApiInfo(), expected.getLineNumber(), expected.getMethodTypeEnum(), expected.getLocation());
    }
}
