/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.realtime.collector.service;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.io.ResponseMessage;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelEventHandler;

import java.util.concurrent.CompletableFuture;

/**
 * @author youngjin.kim2
 */
public interface AgentConnection {

    ClientStreamChannel requestStream(ClientStreamChannelEventHandler handler, GeneratedMessageV3 command);

    CompletableFuture<ResponseMessage> request(GeneratedMessageV3 command);

}
