package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.config.DistributorConfiguration;
import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.MetadataEncoder;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.KeyValue;
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
        Cell cell = CellUtil.createCell(HConstants.EMPTY_BYTE_ARRAY, HConstants.EMPTY_BYTE_ARRAY, bufferArray, HConstants.LATEST_TIMESTAMP, KeyValue.Type.Maximum.getCode(), valueArray);

        Result mockedResult = mock(Result.class);
        when(mockedResult.rawCells()).thenReturn(new Cell[] { cell });
        when(mockedResult.getRow()).thenReturn(rowKey);

        ApiMetaDataMapper dut = new ApiMetaDataMapper(givenRowKeyDistributorByHashPrefix);
        ApiMetaDataBo actual = dut.mapRow(mockedResult, 0).get(0);

        assertThat(actual).extracting("agentId", "startTime", "apiId", "apiInfo", "lineNumber", "methodTypeEnum", "location")
                .contains(expected.getAgentId(), expected.getAgentStartTime(), expected.getId(), expected.getApiInfo(), expected.getLineNumber(), expected.getMethodTypeEnum(), expected.getLocation());
    }
}
