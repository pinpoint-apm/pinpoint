package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.config.DistributorConfiguration;
import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
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
        HbaseOperations2 mockedHbaseTemplate = mock(HbaseOperations2.class);
        TableNameProvider mockedProvider = mock(TableNameProvider.class);
        DistributorConfiguration givenConfiguration = new DistributorConfiguration();
        RowKeyDistributorByHashPrefix givenRowKeyDistributorByHashPrefix = givenConfiguration.metadataRowKeyDistributor();
        HbaseApiMetaDataDao dut = new HbaseApiMetaDataDao(mockedHbaseTemplate, mockedProvider, givenRowKeyDistributorByHashPrefix);

        doAnswer((invocation) -> {
            Put actual = invocation.getArgument(1);
            List<Cell> actualCells = actual.get(HbaseColumnFamily.API_METADATA_API.getName(), HbaseColumnFamily.API_METADATA_API.QUALIFIER_SIGNATURE);
            assertThat(actualCells).hasSize(1);
            return null;
        }).when(mockedHbaseTemplate).put(any(), any(Put.class));

        ApiMetaDataBo stub = new ApiMetaDataBo.Builder("express-node-sample-id", 1669280767548L, 12, 169, MethodTypeEnum.DEFAULT, "express.Function.proto.get(path, callback)")
                .setLocation("/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/routes/index.js")
                .build();
        dut.insert(stub);
    }
}