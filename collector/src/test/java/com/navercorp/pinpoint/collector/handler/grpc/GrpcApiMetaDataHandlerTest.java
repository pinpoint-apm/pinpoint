package com.navercorp.pinpoint.collector.handler.grpc;

import com.navercorp.pinpoint.collector.service.ApiMetaDataService;
import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PApiMetaData;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class GrpcApiMetaDataHandlerTest {
    // ApiMetaDataBo{agentId='express-node-sample-id', startTime=1668495162817, apiId=11, apiInfo='express.Function.proto.get(path, callback)', lineNumber=177, methodTypeEnum=DEFAULT}
    // from Node agent [11, 'express.Function.proto.get(path, callback)', 24, null, '/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/routes/index.js']
    @Test
    public void stubToApiMetaData() {
        ApiMetaDataService mockedService = mock(ApiMetaDataService.class);
        GrpcApiMetaDataHandler dut = new GrpcApiMetaDataHandler(mockedService);

        PApiMetaData actualStub = PApiMetaData.newBuilder()
                .setApiId(13)
                .setApiInfo("express.Function.proto.get(path, callback)")
                .setLine(177)
                .setType(MethodTypeEnum.DEFAULT.getCode())
                .setLocation("/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/routes/index.js")
                .build();
        try (MockedStatic<ServerContext> mocked = mockStatic(ServerContext.class)) {
            mocked.when(ServerContext::getAgentInfo).thenReturn(new Header("name", "express-node-sample-id", "agentName", "applicationName", 0, 1668495162817L, 0, Collections.emptyList()));
            doAnswer((invocation) -> {
                ApiMetaDataBo actual = invocation.getArgument(0);
                assertThat(actual).extracting("agentId", "startTime", "apiId", "apiInfo", "lineNumber", "methodTypeEnum", "location")
                                .contains("express-node-sample-id", 1668495162817L, 13, "express.Function.proto.get(path, callback)", 177, MethodTypeEnum.DEFAULT, "/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/routes/index.js");
                return null;
            }).when(mockedService).insert(any());

            dut.handleApiMetaData(actualStub);

            mocked.verify(ServerContext::getAgentInfo);
        }
    }
}
