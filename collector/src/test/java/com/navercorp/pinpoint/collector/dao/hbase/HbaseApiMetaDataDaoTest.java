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

package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.config.DistributorConfiguration;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.MetaDataRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.MetadataEncoder;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Put;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class HbaseApiMetaDataDaoTest {

    // from node: ApiMetaDataBo{agentId='express-node-sample-id', startTime=1669280767548, apiId=12, apiInfo='express.Function.proto.get(path, callback)', lineNumber=169, methodTypeEnum=DEFAULT, location='/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/routes/index.js'}
    @Test
    public void testInsert() {
        HbaseOperations mockedHbaseTemplate = mock(HbaseOperations.class);
        TableNameProvider mockedProvider = mock(TableNameProvider.class);

        RowKeyEncoder<MetaDataRowKey> rowKeyEncoder = new MetadataEncoder();
        DistributorConfiguration givenConfiguration = new DistributorConfiguration();
        RowKeyDistributorByHashPrefix givenRowKeyDistributorByHashPrefix = givenConfiguration.metadataRowKeyDistributor();
        HbaseApiMetaDataDao dut = new HbaseApiMetaDataDao(mockedHbaseTemplate, rowKeyEncoder, mockedProvider, givenRowKeyDistributorByHashPrefix);

        doAnswer((invocation) -> {
            Put actual = invocation.getArgument(1);
            List<Cell> actualCells = actual.get(HbaseTables.API_METADATA_API.getName(), HbaseTables.API_METADATA_API.QUALIFIER_SIGNATURE);
            assertThat(actualCells).hasSize(1);
            return null;
        }).when(mockedHbaseTemplate).put(any(), any(Put.class));

        ApiMetaDataBo stub = new ApiMetaDataBo.Builder("express-node-sample-id", 1669280767548L, 12, 169, MethodTypeEnum.DEFAULT, "express.Function.proto.get(path, callback)")
                .setLocation("/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/routes/index.js")
                .build();
        dut.insert(stub);
    }
}