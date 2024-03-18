package com.navercorp.pinpoint.collector.handler.grpc;

import com.navercorp.pinpoint.collector.service.ApiMetaDataService;
import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PApiMetaData;
import com.navercorp.pinpoint.grpc.trace.PResult;
import io.grpc.Context;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

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

        Header header = new Header("name", AgentId.of("express-node-sample-id"), "agentName", "applicationName", "serviceName", 0, 1668495162817L, 0, Collections.emptyList());
        Context headerContext = Context.current().withValue(ServerContext.AGENT_INFO_KEY, header);
        headerContext.run(new Runnable() {
            @Override
            public void run() {
                doAnswer((invocation) -> {
                    ApiMetaDataBo actual = invocation.getArgument(0);
                    assertThat(actual).extracting("agentId", "startTime", "apiId", "apiInfo", "lineNumber", "methodTypeEnum", "location")
                            .contains("express-node-sample-id", 1668495162817L, 13, "express.Function.proto.get(path, callback)", 177, MethodTypeEnum.DEFAULT, "/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/routes/index.js");
                    return null;
                }).when(mockedService).insert(any());

                PResult result = dut.handleApiMetaData(actualStub);
                assertThat(result.getSuccess()).isTrue();
            }
        });
    }

}
