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

import com.google.protobuf.StringValue;
import com.navercorp.pinpoint.collector.cluster.ProfilerClusterManager;
import com.navercorp.pinpoint.collector.util.RequestManager;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.grpc.trace.PCmdEcho;
import com.navercorp.pinpoint.grpc.trace.PCmdEchoResponse;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.grpc.trace.PCmdResponse;
import com.navercorp.pinpoint.io.ResponseMessage;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.thrift.io.TCommandType;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Taejin Koo
 */
public class PinpointGrpcServerTest {

    public static Timer testTimer = null;

    private final ClusterKey clusterKey = new ClusterKey("applicationName", "agentid", System.currentTimeMillis());

    private final PCmdEcho request = PCmdEcho.newBuilder().setMessage("hello").build();

    @BeforeAll
    public static void setUp() throws Exception {
        ThreadFactory threadFactory = new PinpointThreadFactory(PinpointGrpcServerTest.class + "-Timer", true);
        testTimer = new HashedWheelTimer(threadFactory, 100, TimeUnit.MILLISECONDS, 512);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        if (testTimer != null) {
            testTimer.stop();
        }
    }

    @Test
    public void stateTest() {
        RecordedStreamObserver recordedStreamObserver = new RecordedStreamObserver();

        RequestManager<ResponseMessage> requestManager = new RequestManager<>(testTimer, 3000);
        PinpointGrpcServer pinpointGrpcServer = new PinpointGrpcServer(Mockito.mock(InetSocketAddress.class), clusterKey, requestManager, Mockito.mock(ProfilerClusterManager.class), recordedStreamObserver);
        assertCurrentState(SocketStateCode.NONE, pinpointGrpcServer);
        CompletableFuture<ResponseMessage> future = pinpointGrpcServer.request(request);
        requestOnInvalidState(future, recordedStreamObserver);

        pinpointGrpcServer.connected();
        assertCurrentState(SocketStateCode.CONNECTED, pinpointGrpcServer);
        future = pinpointGrpcServer.request(request);
        requestOnInvalidState(future, recordedStreamObserver);

        List<Integer> supportCommandList = List.of(Short.toUnsignedInt(TCommandType.ECHO.getCode()));
        pinpointGrpcServer.handleHandshake(supportCommandList);
        assertCurrentState(SocketStateCode.RUN_DUPLEX, pinpointGrpcServer);

        pinpointGrpcServer.disconnected();
        assertCurrentState(SocketStateCode.CLOSED_BY_CLIENT, pinpointGrpcServer);
        future = pinpointGrpcServer.request(request);
        requestOnInvalidState(future, recordedStreamObserver);
    }

    private void requestOnInvalidState(CompletableFuture<ResponseMessage> future, RecordedStreamObserver recordedStreamObserver) {
        Assertions.assertThrows(ExecutionException.class, () -> future.get(3000, TimeUnit.MILLISECONDS));
        Assertions.assertEquals(0, recordedStreamObserver.getRequestCount());
    }

    @Test
    public void requestTest() {
        RecordedStreamObserver<PCmdRequest> recordedStreamObserver = new RecordedStreamObserver<PCmdRequest>();

        PinpointGrpcServer pinpointGrpcServer = new PinpointGrpcServer(Mockito.mock(InetSocketAddress.class), clusterKey, new RequestManager(testTimer, 3000), Mockito.mock(ProfilerClusterManager.class), recordedStreamObserver);
        pinpointGrpcServer.connected();

        List<Integer> supportCommandList = List.of(Short.toUnsignedInt(TCommandType.ECHO.getCode()));
        pinpointGrpcServer.handleHandshake(supportCommandList);

        CompletableFuture<ResponseMessage> future = pinpointGrpcServer.request(this.request);
        Assertions.assertEquals(1, recordedStreamObserver.getRequestCount());
        // timeout
        awaitAndAssert(future, false);

        future = pinpointGrpcServer.request(this.request);
        Assertions.assertEquals(2, recordedStreamObserver.getRequestCount());

        PCmdRequest latestRequest = recordedStreamObserver.getLatestRequest();
        pinpointGrpcServer.handleMessage(latestRequest.getRequestId(), PCmdEchoResponse.newBuilder().setMessage(latestRequest.getCommandEcho().getMessage()).build());
        // success
        awaitAndAssert(future, true);

        future = pinpointGrpcServer.request(this.request);
        Assertions.assertEquals(3, recordedStreamObserver.getRequestCount());
        latestRequest = recordedStreamObserver.getLatestRequest();

        PCmdResponse.Builder builder = PCmdResponse.newBuilder();
        PCmdResponse response = builder.setMessage(StringValue.of("fail")).setResponseId(latestRequest.getRequestId()).build();
        pinpointGrpcServer.handleFail(response);
        // fail
        awaitAndAssert(future, false);

        pinpointGrpcServer.close();
        assertCurrentState(SocketStateCode.CLOSED_BY_SERVER, pinpointGrpcServer);
    }

    private void awaitAndAssert(CompletableFuture<ResponseMessage> future, boolean expected) {
        boolean result;
        try {
            future.get(3000, TimeUnit.MILLISECONDS);
            result = true;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            result = false;
        }
        Assertions.assertEquals(expected, result);
    }

    private void assertCurrentState(SocketStateCode expectedStateCode, PinpointGrpcServer pinpointGrpcServer) {
        SocketStateCode currentState = pinpointGrpcServer.getState();
        Assertions.assertEquals(expectedStateCode, currentState);
    }

}
