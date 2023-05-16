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

package com.navercorp.pinpoint.collector.cluster.route;

import com.navercorp.pinpoint.collector.cluster.GrpcAgentConnection;
import com.navercorp.pinpoint.collector.receiver.grpc.PinpointGrpcServer;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.command.TCommandEcho;
import com.navercorp.pinpoint.thrift.io.TCommandType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class GrpcAgentConnectionTest {

    @Test
    public void requestTest() {
        PinpointGrpcServer mockGrpcServer = Mockito.mock(PinpointGrpcServer.class);

        List<Integer> supportCommandList = List.of(Short.toUnsignedInt(TCommandType.ECHO.getCode()));
        GrpcAgentConnection grpcAgentConnection = new GrpcAgentConnection(mockGrpcServer, supportCommandList);

        boolean supportCommand = grpcAgentConnection.isSupportCommand(TCommandType.TRANSFER.getBodyFactory().getObject());
        Assertions.assertFalse(supportCommand);

        supportCommand = grpcAgentConnection.isSupportCommand(TCommandType.RESULT.getBodyFactory().getObject());
        Assertions.assertFalse(supportCommand);

        supportCommand = grpcAgentConnection.isSupportCommand(TCommandType.ECHO.getBodyFactory().getObject());
        Assertions.assertTrue(supportCommand);

        final CompletableFuture<ResponseMessage> future = grpcAgentConnection.request(new TResult());

        Assertions.assertThrows(Exception.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                future.get(3000, TimeUnit.MILLISECONDS);
            }
        });
        TCommandEcho commandEcho = new TCommandEcho("hello");
        // check to pass validation
        final CompletableFuture<ResponseMessage> future2 = grpcAgentConnection.request(commandEcho);
        Assertions.assertNull(future2);
    }

    @Test
    public void equalsTest() {
        PinpointGrpcServer mockGrpcServer1 = Mockito.mock(PinpointGrpcServer.class);

        List<Integer> supportCommandList = List.of(Short.toUnsignedInt(TCommandType.ECHO.getCode()));
        GrpcAgentConnection grpcAgentConnection = new GrpcAgentConnection(mockGrpcServer1, supportCommandList);

        Assertions.assertEquals(grpcAgentConnection, new GrpcAgentConnection(mockGrpcServer1, supportCommandList));

        PinpointGrpcServer mockGrpcServer2 = Mockito.mock(PinpointGrpcServer.class);
        Assertions.assertNotEquals(grpcAgentConnection, new GrpcAgentConnection(mockGrpcServer2, supportCommandList));
    }

}
