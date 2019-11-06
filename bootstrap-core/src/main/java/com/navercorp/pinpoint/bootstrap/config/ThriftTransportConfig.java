/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.config;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface ThriftTransportConfig {

    String getCollectorSpanServerIp();

    int getCollectorSpanServerPort();

    String getCollectorStatServerIp();

    int getCollectorStatServerPort();

    String getCollectorTcpServerIp();

    int getCollectorTcpServerPort();

    int getStatDataSenderWriteQueueSize();

    int getStatDataSenderSocketSendBufferSize();

    int getStatDataSenderSocketTimeout();

    String getStatDataSenderSocketType();

    String getStatDataSenderTransportType();

    String getStatDataSenderWriteBufferHighWaterMark();

    String getStatDataSenderWriteBufferLowWaterMark();


    int getSpanDataSenderWriteQueueSize();

    int getSpanDataSenderSocketSendBufferSize();

    boolean isTcpDataSenderCommandAcceptEnable();

    boolean isTcpDataSenderCommandActiveThreadEnable();

    boolean isTcpDataSenderCommandActiveThreadCountEnable();



    boolean isTcpDataSenderCommandActiveThreadDumpEnable();

    boolean isTcpDataSenderCommandActiveThreadLightDumpEnable();

    long getTcpDataSenderPinpointClientWriteTimeout();

    long getTcpDataSenderPinpointClientRequestTimeout();

    long getTcpDataSenderPinpointClientReconnectInterval();


    long getTcpDataSenderPinpointClientPingInterval();

    long getTcpDataSenderPinpointClientHandshakeInterval();

    String getTcpDataSenderPinpointClientWriteBufferHighWaterMark();

    String getTcpDataSenderPinpointClientWriteBufferLowWaterMark();

    int getSpanDataSenderSocketTimeout();

    String getSpanDataSenderSocketType();

    String getSpanDataSenderTransportType();

    String getSpanDataSenderWriteBufferHighWaterMark();

    String getSpanDataSenderWriteBufferLowWaterMark();

    int getSpanDataSenderChunkSize();

    int getStatDataSenderChunkSize();

}
