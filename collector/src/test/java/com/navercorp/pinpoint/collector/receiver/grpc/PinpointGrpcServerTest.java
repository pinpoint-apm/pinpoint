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

package com.navercorp.pinpoint.collector.receiver.grpc;

import com.navercorp.pinpoint.collector.cluster.AgentInfo;
import com.navercorp.pinpoint.grpc.trace.PCmdEcho;
import com.navercorp.pinpoint.grpc.trace.PCmdEchoResponse;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.grpc.trace.PCmdResponse;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.RequestManager;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import com.navercorp.pinpoint.thrift.io.TCommandType;

import com.google.protobuf.StringValue;
import org.jboss.netty.util.Timer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class PinpointGrpcServerTest {

    public static Timer testTimer = null;

    private final AgentInfo agentInfo = new AgentInfo("applicationName", "agentid", System.currentTimeMillis());

    private final PCmdEcho request = PCmdEcho.newBuilder().setMessage("hello").build();

    @BeforeClass
    public static void setUp() throws Exception {
        testTimer = TimerFactory.createHashedWheelTimer(PinpointGrpcServerTest.class + "-Timer", 100, TimeUnit.MILLISECONDS, 512);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (testTimer != null) {
            testTimer.stop();
        }
    }

    @Test
    public void stateTest() {
        RecordedStreamObserver recordedStreamObserver = new RecordedStreamObserver();

        PinpointGrpcServer pinpointGrpcServer = new PinpointGrpcServer(Mockito.mock(InetSocketAddress.class), agentInfo, new RequestManager(testTimer, 3000), recordedStreamObserver);
        assertCurrentState(SocketStateCode.NONE, pinpointGrpcServer);
        Future<ResponseMessage> future = pinpointGrpcServer.request(request);
        requestOnInvalidState(future, recordedStreamObserver);

        pinpointGrpcServer.connected();
        assertCurrentState(SocketStateCode.CONNECTED, pinpointGrpcServer);
        future = pinpointGrpcServer.request(request);
        requestOnInvalidState(future, recordedStreamObserver);

        List<Integer> supportCommandList = Collections.singletonList(Short.toUnsignedInt(TCommandType.ECHO.getCode()));
        pinpointGrpcServer.handleHandshake(supportCommandList);
        assertCurrentState(SocketStateCode.RUN_DUPLEX, pinpointGrpcServer);

        pinpointGrpcServer.disconnected();
        assertCurrentState(SocketStateCode.CLOSED_BY_CLIENT, pinpointGrpcServer);
        future = pinpointGrpcServer.request(request);
        requestOnInvalidState(future, recordedStreamObserver);
    }

    private void requestOnInvalidState(Future<ResponseMessage> future, RecordedStreamObserver recordedStreamObserver) {
        Assert.assertFalse(future.isSuccess());
        Assert.assertTrue(future.getCause() instanceof IllegalStateException);
        Assert.assertEquals(0, recordedStreamObserver.getRequestCount());
    }

    @Test
    public void requestTest() {
        RecordedStreamObserver<PCmdRequest> recordedStreamObserver = new RecordedStreamObserver<PCmdRequest>();

        PinpointGrpcServer pinpointGrpcServer = new PinpointGrpcServer(Mockito.mock(InetSocketAddress.class), agentInfo, new RequestManager(testTimer, 3000), recordedStreamObserver);
        pinpointGrpcServer.connected();

        List<Integer> supportCommandList = Collections.singletonList(Short.toUnsignedInt(TCommandType.ECHO.getCode()));
        pinpointGrpcServer.handleHandshake(supportCommandList);

        Future<ResponseMessage> future = pinpointGrpcServer.request(this.request);
        Assert.assertEquals(1, recordedStreamObserver.getRequestCount());
        // timeout
        awaitAndAssert(future, false);

        future = pinpointGrpcServer.request(this.request);
        Assert.assertEquals(2, recordedStreamObserver.getRequestCount());

        PCmdRequest latestRequest = recordedStreamObserver.getLatestRequest();
        pinpointGrpcServer.handleMessage(latestRequest.getRequestId(), PCmdEchoResponse.newBuilder().setMessage(latestRequest.getCommandEcho().getMessage()).build());
        // success
        awaitAndAssert(future, true);

        future = pinpointGrpcServer.request(this.request);
        Assert.assertEquals(3, recordedStreamObserver.getRequestCount());
        latestRequest = recordedStreamObserver.getLatestRequest();

        PCmdResponse.Builder builder = PCmdResponse.newBuilder();
        PCmdResponse response = builder.setMessage(StringValue.of("fail")).setResponseId(latestRequest.getRequestId()).build();
        pinpointGrpcServer.handleFail(response);
        // fail
        awaitAndAssert(future, false);

        pinpointGrpcServer.close();
        assertCurrentState(SocketStateCode.CLOSED_BY_SERVER, pinpointGrpcServer);
    }

    private void awaitAndAssert(Future<ResponseMessage> future, boolean expected) {
        future.await();
        // timeout

        Assert.assertEquals(expected, future.isSuccess());
    }

    private void assertCurrentState(SocketStateCode expectedStateCode, PinpointGrpcServer pinpointGrpcServer) {
        SocketStateCode currentState = pinpointGrpcServer.getState();
        Assert.assertEquals(expectedStateCode, currentState);
    }

}
